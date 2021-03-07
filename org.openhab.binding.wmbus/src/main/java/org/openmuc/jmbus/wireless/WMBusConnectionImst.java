/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.wireless;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.transportlayer.TransportLayer;

/**
 * Was tested with the IMST iM871A-USB Wireless M-Bus stick.<br>
 */
class WMBusConnectionImst extends AbstractWMBusConnection {

    private class MessageReceiverImpl extends MessageReceiver {

        private static final byte MBUS_BL_CONTROL = 0x44;
        private final TransportLayer transportLayer;

        public MessageReceiverImpl(TransportLayer transportLayer, WMBusListener listener) {
            super(listener);
            this.transportLayer = transportLayer;
        }

        @Override
        public void run() {
            try {

                while (!isClosed()) {
                    try {
                        task();
                    } catch (InterruptedIOException e) {
                        // ignore a timeout..
                    } catch (HciMessageException e) {
                        super.notifyDiscarded(e.getData());
                    }
                }

            } catch (IOException e) {
                if (isClosed()) {
                    return;
                }

                super.notifyStoppedListening(e);
            } finally {
                close();
                super.shutdown();
            }
        }

        private void task() throws IOException {
            HciMessage hciMessage = readHciMsg();

            final byte[] wmbusMessage = hciMessage.getPayload();
            final int signalStrengthInDBm = hciMessage.getRSSI();
            try {
                super.notifyNewMessage(WMBusMessage.decode(wmbusMessage, signalStrengthInDBm, keyMap));
            } catch (DecodingException e) {
                super.notifyDiscarded(wmbusMessage);
            }
        }

        private HciMessage readHciMsg() throws IOException {
            while (true) {
                HciMessage hciMessage = HciMessage.decode(this.transportLayer);

                if (hciMessage.getPayload().length <= 1) {
                    continue;
                }

                if (hciMessage.getPayload()[1] == MBUS_BL_CONTROL) {
                    return hciMessage;
                } else {
                    discard(hciMessage);
                }
            }
        }

        private void discard(HciMessage hciMessage) {
            super.notifyDiscarded(hciMessage.payload);
        }
    }

    public WMBusConnectionImst(WMBusMode mode, WMBusListener listener, TransportLayer tl) {
        super(mode, listener, tl);
    }

    @Override
    protected MessageReceiver newMessageReceiver(TransportLayer transportLayer, WMBusListener listener) {
        return new MessageReceiverImpl(transportLayer, listener);
    }

    @Override
    protected void initializeWirelessTransceiver(WMBusMode mode) throws IOException {

        byte[] payload = ByteBuffer.allocate(6).put((byte) 0x00) // NVM Flag: change configuration only temporary
                .put((byte) 0x03) // IIFlag 1: Bit 0 Device Mode and Bit 1 Radio Mode
                .put((byte) 0x00) // Device Mode: Meter
                .put(linkRadioModeFor(mode)) // Link/Radio Mode
                .put((byte) 0x10) // IIFlag 2: Bit 4 : Auto RSSI Attachment
                .put((byte) 0x01) // Rx-Timestamp attached for each received Radio message
                .array();

        writeCommand(Const.DEVMGMT_ID, Const.DEVMGMT_MSG_SET_CONFIG_REQ, payload);
    }

    private static byte linkRadioModeFor(WMBusMode mode) throws IOException {
        switch (mode) {
            case S:
                return 0x01; // Link/Radio Mode: S1-m
            case T:
                return 0x04; // Link/Radio Mode: T2
            case C:
                return 0x08; // Link/Radio Mode: C2 with telegram format A (C2 + format B = 0x09)
            default:
                String msg = MessageFormat.format("wMBUS Mode ''{0}'' is not supported", mode.toString());
                throw new IOException(msg);
        }
    }

    private boolean writeCommand(byte endpointId, byte msgId, byte[] payload) {
        byte controlField = 0;
        int lengthPayloadAll = payload.length & 0xFF;
        double numOfPackagesTemp = (double) lengthPayloadAll / Const.MAX_SINGLE_PAYLOAD_SIZE;

        int numOfPackages = (int) numOfPackagesTemp;
        int comma = (int) (numOfPackagesTemp - numOfPackages) * 100;
        if (numOfPackages == 0 || comma != 0) {
            ++numOfPackages;
        }

        if (numOfPackages > Const.MAX_PACKAGES) {
            return false;
        }

        for (int i = 0; i < numOfPackages; ++i) {
            int payloadSendLength = Const.MAX_SINGLE_PAYLOAD_SIZE;
            if (numOfPackages - i == 1) {
                payloadSendLength = lengthPayloadAll - (numOfPackages - 1) * Const.MAX_SINGLE_PAYLOAD_SIZE;
            }
            byte[] payloadSend = new byte[payloadSendLength];
            System.arraycopy(payload, i * payloadSendLength, payloadSend, 0, payloadSendLength);

            byte controlField_EndpointField = (byte) ((controlField << 4) | endpointId & 0xff);
            byte[] hciHeader = { Const.START_OF_FRAME, controlField_EndpointField, msgId, (byte) payloadSendLength };

            byte[] hciMessage = new byte[Const.HCI_HEADER_LENGTH + payloadSendLength];
            System.arraycopy(hciHeader, 0, hciMessage, 0, hciHeader.length);
            System.arraycopy(payloadSend, 0, hciMessage, hciHeader.length, payloadSend.length);

            try {
                getOutputStream().write(hciMessage);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Writes a reset command to the IMST module
     */
    public void reset() {
        writeCommand(Const.DEVMGMT_MSG_FACTORY_RESET_REQ, Const.DEVMGMT_ID, new byte[0]);
    }

    /**
     * IMST constants packages
     */
    class Const {
        public static final byte START_OF_FRAME = (byte) 0xA5;
        // A5 01 03
        public static final int MAX_PACKAGES = 255;
        public static final int MAX_SINGLE_PAYLOAD_SIZE = 255;
        public static final int HCI_HEADER_LENGTH = 4;

        // ControlField
        public static final byte RESERVED = 0x00; // 0b0000
        public static final byte TIMESTAMP_ATTACHED = 0x02; // 0b0010
        public static final byte RSSI_ATTACHED = 0x04; // 0b0100
        public static final byte CRC16_ATTACHED = 0x08; // 0b1000 (FCS)

        // List of Endpoint Identifier
        public static final byte DEVMGMT_ID = 0x01;
        public static final byte RADIOLINK_ID = 0x02;
        public static final byte RADIOLINKTEST_ID = 0x03;
        public static final byte HWTEST_ID = 0x04;

        // Device Management MessageIdentifier
        public static final byte DEVMGMT_MSG_PING_REQ = 0x01;
        public static final byte DEVMGMT_MSG_PING_RSP = 0x02;
        public static final byte DEVMGMT_MSG_SET_CONFIG_REQ = 0x03;
        public static final byte DEVMGMT_MSG_SET_CONFIG_RSP = 0x04;
        public static final byte DEVMGMT_MSG_GET_CONFIG_REQ = 0x05;
        public static final byte DEVMGMT_MSG_GET_CONFIG_RSP = 0x06;
        public static final byte DEVMGMT_MSG_RESET_REQ = 0x07;
        public static final byte DEVMGMT_MSG_RESET_RSP = 0x08;
        public static final byte DEVMGMT_MSG_FACTORY_RESET_REQ = 0x09;
        public static final byte DEVMGMT_MSG_FACTORY_RESET_RSP = 0x0A;
        public static final byte DEVMGMT_MSG_GET_OPMODE_REQ = 0x0B;
        public static final byte DEVMGMT_MSG_GET_OPMODE_RSP = 0x0C;
        public static final byte DEVMGMT_MSG_SET_OPMODE_REQ = 0x0D;
        public static final byte DEVMGMT_MSG_SET_OPMODE_RSP = 0x0E;
        public static final byte DEVMGMT_MSG_GET_DEVICEINFO_REQ = 0x0F;
        public static final byte DEVMGMT_MSG_GET_DEVICEINFO_RSP = 0x10;
        public static final byte DEVMGMT_MSG_GET_SYSSTATUS_REQ = 0x11;
        public static final byte DEVMGMT_MSG_GET_SYSSTATUS_RSP = 0x12;
        public static final byte DEVMGMT_MSG_GET_FWINFO_REQ = 0x13;
        public static final byte DEVMGMT_MSG_GET_FWINFO_RSP = 0x14;
        public static final byte DEVMGMT_MSG_GET_RTC_REQ = 0x19;
        public static final byte DEVMGMT_MSG_GET_RTC_RSP = 0x1A;
        public static final byte DEVMGMT_MSG_SET_RTC_REQ = 0x1B;
        public static final byte DEVMGMT_MSG_SET_RTC_RSP = 0x1C;
        public static final byte DEVMGMT_MSG_ENTER_LPM_REQ = 0x1D;
        public static final byte DEVMGMT_MSG_ENTER_LPM_RSP = 0x1E;
        public static final byte DEVMGMT_MSG_SET_AES_ENCKEY_REQ = 0x21;
        public static final byte DEVMGMT_MSG_SET_AES_ENCKEY_RSP = 0x22;
        public static final byte DEVMGMT_MSG_ENABLE_AES_ENCKEY_REQ = 0x23;
        public static final byte DEVMGMT_MSG_ENABLE_AES_ENCKEY_RSP = 0x24;
        public static final byte DEVMGMT_MSG_SET_AES_DECKEY_RSP_0X25 = 0x25;
        public static final byte DEVMGMT_MSG_SET_AES_DECKEY_RSP_0X26 = 0x26;
        public static final byte DEVMGMT_MSG_AES_DEC_ERROR_IND = 0x27;

        // Radio Link Message Identifier
        public static final byte RADIOLINK_MSG_WMBUSMSG_REQ = 0x01;
        public static final byte RADIOLINK_MSG_WMBUSMSG_RSP = 0x02;
        public static final byte RADIOLINK_MSG_WMBUSMSG_IND = 0x03;
        public static final byte RADIOLINK_MSG_DATA_REQ = 0x04;
        public static final byte RADIOLINK_MSG_DATA_RSP = 0x05;
    }

    /**
     * <li><tt>HCI Message</tt>
     * <ul>
     * <li>StartOfFrame 8 Bit: 0xA5
     * <li>MsgHeader 24 Bit:
     * <ul>
     * <li>ControlField 4 Bit:
     * <ul>
     * <li>0000b Reserved
     * <li>0010b Time Stamp Field attached
     * <li>0100b RSSI Field attached
     * <li>1000b CRC16 Field attached
     * </ul>
     * <li>EndPoint ID 4 Bit: Identifies a logical message endpoint which groups several messages.
     * <li>Msg ID Field 8 Bit: Identifies the message type.
     * <li>LengthFiled 8 Bit: Number of bytes in the payload. If null no payload.
     * </ul>
     * <li>PayloadField n * 8 Bit: wMBus Message
     * <li>Time Stamp (optional): 32 Bit Timestamp of the RTC
     * <li>RSSI (optional) 8 Bit: Receive Signal Strength Indicator
     * <li>FCS (optional) 16 Bit: CRC from Control Field up to last byte of Payload, Time Stamp or RSSI Field.</li>
     * </ul>
     *
     */
    private static class HciMessage {

        private final byte controlField;;
        private final byte endpointID;
        private final byte msgId;
        private final int length;

        private final byte[] payload;
        private final int timeStamp;
        private final int rSSI;
        private final int fCS;

        private HciMessage(byte controlField, byte endpointID, byte msgId, int length, byte[] payload, int timeStamp,
                int rSSI, int fCS) {
            this.controlField = controlField;
            this.endpointID = endpointID;
            this.msgId = msgId;
            this.length = length;
            this.payload = payload;
            this.timeStamp = timeStamp;
            this.rSSI = rSSI;
            this.fCS = fCS;
        }

        public static HciMessage decode(TransportLayer transportLayer) throws IOException {
            DataInputStream is = transportLayer.getInputStream();
            byte b0, b1;

            transportLayer.setTimeout(0);
            b0 = is.readByte();
            if (b0 != Const.START_OF_FRAME) {
                String msg = String.format("First byte does not start with %02X.", Const.START_OF_FRAME);
                throw new IOException(msg);
            }

            transportLayer.setTimeout(MESSAGE_FRAGEMENT_TIMEOUT);
            // this may time out.
            b1 = is.readByte();

            byte controlField = (byte) ((b1 >> 4) & 0x0F);
            byte endpointId = (byte) (b1 & 0x0F);

            byte msgId = is.readByte();
            int length = is.readUnsignedByte();

            byte[] payload = readPayload(is, length);

            int timeStamp = 0;
            if ((controlField & Const.TIMESTAMP_ATTACHED) == Const.TIMESTAMP_ATTACHED) {
                timeStamp = is.readInt();
            }

            int rSSI = 0;
            if ((controlField & Const.RSSI_ATTACHED) == Const.RSSI_ATTACHED) {
                double b = -100.0 - (4000.0 / 150.0);
                double m = 80.0 / 150.0;
                rSSI = (int) (m * is.readUnsignedByte() + b);
            }

            int fCS = 0;
            if ((controlField & Const.CRC16_ATTACHED) == Const.CRC16_ATTACHED) {
                fCS = is.readUnsignedShort();
            }

            return new HciMessage(controlField, endpointId, msgId, length, payload, timeStamp, rSSI, fCS);
        }

        private static byte[] readPayload(DataInputStream is, final int length) throws IOException {
            byte[] payload = new byte[length + 1];
            payload[0] = (byte) length;

            int readLength = is.read(payload, 1, length);

            if (readLength != length) {
                byte[] data = Arrays.copyOfRange(payload, 1, 1 + readLength);
                throw new HciMessageException(data);
            }

            return payload;
        }

        @Override
        public String toString() {
            return new StringBuilder().append("Control Field: ").append(byteAsHexString(controlField))
                    .append("\nEndpointID:    ").append(byteAsHexString(endpointID)).append("\nMsg ID:        ")
                    .append(byteAsHexString(msgId)).append("\nLength:        ").append(length)
                    .append("\nTimestamp:     ").append(timeStamp).append("\nRSSI:          ").append(rSSI)
                    .append("\nFCS:           ").append(fCS).append("\nPayload:\n")
                    .append(DatatypeConverter.printHexBinary(payload)).toString();
        }

        private static String byteAsHexString(byte b) {
            return String.format("%02X", b);
        }

        public byte[] getPayload() {
            return payload;
        }

        public int getRSSI() {
            return rSSI;
        }
    }
}
