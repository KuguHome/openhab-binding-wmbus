/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.wireless;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.util.Arrays;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.transportlayer.TransportLayer;

/*
 * Radio Craft W-MBUS frame:
 * 
 * @formatter:off
 * +---+----+-----------+
 * | L | CI | APPL_DATA |
 * +---+----+-----------+
 *  ~ L is the length (not including the length byte itself)
 *  ~ CI is the Control Information byte
 * @formatter:on
 */
class WMBusConnectionRadioCrafts extends AbstractWMBusConnection {

    private class MessageReceiverImpl extends MessageReceiver {

        /**
         * Indicates message from primary station, function send/no reply (SND -N
         */
        private static final byte CONTROL_BYTE = 0x44;
        private final TransportLayer transportLayer;

        private final byte[] discardBuffer = new byte[BUFFER_LENGTH];
        private int bufferPointer = 0;

        public MessageReceiverImpl(TransportLayer transportLayer, WMBusListener listener) {
            super(listener);
            this.transportLayer = transportLayer;
        }

        @Override
        public void run() {
            try {

                while (!isClosed()) {

                    byte[] messageData = initMessageData();

                    int len = messageData.length - 2;
                    handleData(messageData, len);

                }
            } catch (final IOException e) {
                if (!isClosed()) {
                    super.notifyStoppedListening(e);
                }

            } finally {
                close();
                super.shutdown();
            }
        }

        private void handleData(byte[] messageData, int len) throws IOException {
            try {
                int numReadBytes = getInputStream().read(messageData, 2, len);

                if (len == numReadBytes) {
                    notifyListener(messageData);
                } else {
                    discard(messageData, 0, numReadBytes + 2);
                }
            } catch (InterruptedIOException e) {
                discard(messageData, 0, 2);
            }
        }

        private byte[] initMessageData() throws IOException {
            byte b0, b1;
            while (true) {
                this.transportLayer.setTimeout(0);
                b0 = getInputStream().readByte();
                this.transportLayer.setTimeout(MESSAGE_FRAGEMENT_TIMEOUT);

                try {
                    // this may time out.
                    b1 = getInputStream().readByte();
                } catch (InterruptedIOException e) {
                    continue;
                }

                if (b1 == CONTROL_BYTE) {
                    break;
                }

                discardBuffer[bufferPointer++] = b0;
                discardBuffer[bufferPointer++] = b1;

                if (bufferPointer - 2 >= discardBuffer.length) {
                    discard(discardBuffer, 0, bufferPointer);
                    bufferPointer = 0;
                }

            }

            int messageLength = b0 & 0xff;

            final byte[] messageData = new byte[messageLength + 1];
            messageData[0] = b0;
            messageData[1] = b1;

            return messageData;
        }

        private void notifyListener(final byte[] messageBytes) {
            messageBytes[0] = (byte) (messageBytes[0] - 1);
            int rssi = messageBytes[messageBytes.length - 1] & 0xff;

            final int signalStrengthInDBm = (rssi * -1) / 2;
            try {
                super.notifyNewMessage(WMBusMessage.decode(messageBytes, signalStrengthInDBm, keyMap));
            } catch (DecodingException e) {
                super.notifyDiscarded(messageBytes);
            }
        }

        private void discard(byte[] buffer, int offset, int length) {
            final byte[] discardedBytes = Arrays.copyOfRange(buffer, offset, offset + length);

            super.notifyDiscarded(discardedBytes);
        }
    }

    public WMBusConnectionRadioCrafts(WMBusMode mode, WMBusListener listener, TransportLayer tl) {
        super(mode, listener, tl);
    }

    @Override
    protected MessageReceiver newMessageReceiver(TransportLayer transportLayer, WMBusListener listener) {
        return new MessageReceiverImpl(transportLayer, listener);
    }

    /**
     * @param mode
     *            - the wMBus mode to be used for transmission
     * @throws IOException
     */
    @Override
    protected void initializeWirelessTransceiver(WMBusMode mode) throws IOException {
        // enter config mode
        sendByteInConfigMode(0x00);

        DataOutputStream os = getOutputStream();
        int modeFlag = getModeFlag(mode);

        init(os, modeFlag);

        // /* Set Auto Answer Register */
        // sendByteInConfigMode(0x41);
        // sendByteInConfigMode(0xff);

        // leave config mode
        os.write(0x58);
        os.flush();
    }

    private int getModeFlag(WMBusMode mode) throws IOException {
        int modeFlag;

        switch (mode) {
            case C:
                modeFlag = 0x04;
                break;
            case S:
                modeFlag = 0x00;
                break;
            case T:
                modeFlag = 0x02;
                break;
            default:
                String msg = MessageFormat.format("wMBUS Mode ''{0}'' is not supported", mode.toString());
                throw new IOException(msg);
        }
        return modeFlag;
    }

    private void init(DataOutputStream os, int modeFlag) throws IOException {
        requestSetMode(os, modeFlag);

        requestSetMasterMode(os);

        requestRssiInformation(os);
    }

    private void requestSetMode(DataOutputStream os, int modeFlag) throws IOException {
        sendRequest(os, 0x03, modeFlag);
    }

    private void requestSetMasterMode(DataOutputStream os) throws IOException {
        sendRequest(os, 0x12, 0x01);
    }

    private void requestRssiInformation(DataOutputStream os) throws IOException {
        sendRequest(os, 0x05, 0x01);
    }

    private void sendRequest(DataOutputStream os, int b0, int b1) throws IOException {
        sendByteInConfigMode(0x4d);
        os.write(b0);
        os.write(b1);
        sendByteInConfigMode(0xff);
    }

    private void sendByteInConfigMode(int b) throws IOException {

        discardNoise();

        DataOutputStream os = getOutputStream();
        os.write(b);
        os.flush();

        waitForAck();
    }
}
