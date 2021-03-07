/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.wireless;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.transportlayer.TransportLayer;

/**
 * Was tested with the Amber 8426M Wireless M-Bus stick.
 */
class WMBusConnectionAmber extends AbstractWMBusConnection {

    private class MessageReceiverImpl extends MessageReceiver {

        private static final int MBUS_BL_CONTROL = 0x44;

        private int discardCount = 0;
        private final TransportLayer transportLayer;

        public MessageReceiverImpl(TransportLayer transportLayer, WMBusListener listener) {
            super(listener);
            this.transportLayer = transportLayer;
        }

        @Override
        public void run() {

            try {

                while (!isClosed()) {
                    task();
                }

            } catch (final IOException e) {
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

            ByteBuffer discardBuffer = ByteBuffer.allocate(100);

            int b0, b1;
            DataInputStream is = getInputStream();
            while (true) {
                try {
                    this.transportLayer.setTimeout(0);
                    b0 = is.read();
                    this.transportLayer.setTimeout(MESSAGE_FRAGEMENT_TIMEOUT);
                    b1 = is.read();

                    if ((b1 ^ MBUS_BL_CONTROL) == 0) {
                        break;
                    }

                    if (discardBuffer.capacity() - discardBuffer.position() < 2) {
                        discard(discardBuffer.array(), 0, discardBuffer.position());
                        discardBuffer.clear();
                    }
                    discardBuffer.put((byte) b0);
                    discardBuffer.put((byte) b1);
                } catch (InterruptedIOException e) {
                    continue;
                }
            }

            int len = (b0 & 0xff) + 1;
            byte[] data = new byte[2 + len];

            data[0] = (byte) b0;
            data[1] = (byte) b1;

            int readLength = len - 2;
            int actualLength = is.read(data, 2, readLength);

            if (readLength != actualLength) {
                discard(data, 0, actualLength);
                return;
            }

            notifyListener(data);

            if (discardBuffer.position() > 0) {
                discard(discardBuffer.array(), 0, discardBuffer.position());
            }
        }

        private void notifyListener(final byte[] data) {
            int rssi = data[data.length - 1] & 0xff;
            final Integer signalStrengthInDBm;
            int rssiOffset = 74;
            if (rssi >= 128) {
                signalStrengthInDBm = ((rssi - 256) / 2) - rssiOffset;
            } else {
                signalStrengthInDBm = (rssi / 2) - rssiOffset;
            }

            data[0] = (byte) (data[0] - 1);

            try {
                super.notifyNewMessage(WMBusMessage.decode(data, signalStrengthInDBm, keyMap));
            } catch (DecodingException e) {
                super.notifyDiscarded(data);
            }
        }

        private void discard(byte[] data, int offset, int length) {
            discardCount++;
            final byte[] discardedBytes = Arrays.copyOfRange(data, offset, offset + length);

            super.notifyDiscarded(discardedBytes);

            if (discardCount >= 5) {
                try {
                    reset();
                } catch (IOException e) {
                    // ignoring reset errors here..
                }
                discardCount = 0;
            }
        }
    }

    public WMBusConnectionAmber(WMBusMode mode, WMBusListener listener, TransportLayer tl) {
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
        switch (mode) {
            case S:
                amberSetReg((byte) 0x46, (byte) 0x03);
                break;
            case T:
                amberSetReg((byte) 0x46, (byte) 0x08); // T2-OTHER (correct for receiving station in T mode)
                break;
            case C:
                amberSetReg((byte) 0x46, (byte) 0x0e); // C2-OTHER
                break;
            default:
                String message = MessageFormat.format("wMBUS Mode ''{0}'' is not supported", mode.toString());
                throw new IOException(message);
        }
        amberSetReg((byte) 0x45, (byte) 0x01); // Enable attaching RSSI to message
    }

    /**
     * Writes a {@code CMD_SET_REQ} to the Amber module.
     * 
     * @param cmd
     *            register address of the Amber module.
     * @param data
     *            new value(s) for this register address(es).
     * @throws IOException
     *             if an error occurred, while writing the command.
     */
    private void writeCommand(byte cmd, byte[] data) throws IOException {
        DataOutputStream os = getOutputStream();

        byte[] header = ByteBuffer.allocate(3).put((byte) 0xFF).put(cmd).put((byte) data.length).array();

        os.write(header);
        os.write(data);

        byte checksum = computeCheckSum(data, computeCheckSum(header, (byte) 0));

        os.write(checksum);
    }

    private void amberSetReg(byte reg, byte value) throws IOException {
        byte[] data = { reg, 0x01, value };

        writeCommand((byte) 0x09, data);

        discardNoise();
    }

    /**
     * Writes a reset command to the Amber module
     * 
     * @throws IOException
     *             if the reset command failed.
     */
    private void reset() throws IOException {
        writeCommand((byte) 0x05, new byte[] {});
    }

    private static byte computeCheckSum(byte[] data, byte checksum) {
        for (byte element : data) {
            checksum = (byte) (checksum ^ element);
        }
        return checksum;
    }
}
