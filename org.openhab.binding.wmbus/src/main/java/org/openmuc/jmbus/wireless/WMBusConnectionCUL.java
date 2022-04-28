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
import java.text.MessageFormat;
import java.util.Arrays;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.HexUtils;
import org.openmuc.jmbus.transportlayer.TransportLayer;

/**
 * Was tested with nanocul CULFW 1.67
 */
class WMBusConnectionCUL extends AbstractWMBusConnection {

    private class MessageReceiverImpl extends MessageReceiver {

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

            int b0;
            DataInputStream is = getInputStream();

            while (true) {
                try {
                    this.transportLayer.setTimeout(0);
                    b0 = is.readByte(); // b = indicator of received message by cul
                    this.transportLayer.setTimeout(MESSAGE_FRAGEMENT_TIMEOUT);

                    if ((b0 ^ 'b') == 0) {
                        // we found beginning of mBUS frame, in the b0 will be the length of message
                        break;
                    }
                } catch (InterruptedIOException e) {
                    continue;
                }
            }

            StringBuilder data = new StringBuilder();
            String lengthInHex = new String(is.readNBytes(2));
            int length = Integer.parseInt(lengthInHex, 16);

            // strip CRCs
            data.append(lengthInHex);
            data.append(new String(is.readNBytes(9 * 2))); // rest of header 9 bytes as hex
            is.readNBytes(2 * 2); // discard CRC after header

            int bytesReadAfterHeader = 0;
            while (bytesReadAfterHeader < length - 9) {
                data.append(new String(is.readNBytes(2)));
                bytesReadAfterHeader++;

                if (bytesReadAfterHeader % 16 == 0 || bytesReadAfterHeader + 9 == length) {
                    is.readNBytes(2 * 2); // discard CRCs
                }
            }

            is.readLine(); // discard CRC after last block until \n finishes the message
            notifyListener(data.toString());
        }

        private void notifyListener(final String data) {
            Integer signalStrengthInDBm = 0; // no information about real strength available
            try {
                super.notifyNewMessage(WMBusMessage.decode(HexUtils.hexToBytes(data), signalStrengthInDBm, keyMap));
            } catch (DecodingException e) {
                // ignore
            }
        }
    }

    public WMBusConnectionCUL(WMBusMode mode, WMBusListener listener, TransportLayer tl) {
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

        try {
            setMode(mode);
        } catch (Exception e) {
            setMode(mode); // try again because cul may react slow
        }
    }

    private void setMode(WMBusMode mode) throws IOException {
        switch (mode) {
            case S:
                writeCommand(new byte[] { 'b', 'r', 's' });
                expectResponse("SMODE".getBytes());
                break;
            case T:
                writeCommand(new byte[] { 'b', 'r', 't' });
                expectResponse("TMODE".getBytes());
                break;
            case C:
                writeCommand(new byte[] { 'b', 'r', 'c' });
                expectResponse("CMODE".getBytes());
                break;
            default:
                String message = MessageFormat.format("wMBUS Mode ''{0}'' is not supported", mode.toString());
                throw new IOException(message);
        }
    }

    /**
     * Writes a {@code command} to the cul module.
     * 
     * @param cmd
     *            cul command
     * @throws IOException
     *             if an error occurred, while writing the command.
     */
    private void writeCommand(byte[] cmd) throws IOException {
        DataOutputStream os = getOutputStream();
        os.write(cmd);
        os.write('\n');
        os.flush();
    }

    /**
     * reads a response of the cul and compares with the expected value
     * 
     * @param cmd
     *            expected response
     * @throws IOException
     *             if an error occurred, while evaluating the command response.
     */
    private void expectResponse(byte[] response) throws IOException {
        byte[] actualResponse = getInputStream().readNBytes(response.length);
        if (!Arrays.equals(response, actualResponse)) {
            throw new IOException(MessageFormat.format(
                    "Setup of CUL failed! Could not set receive mode. Expected response was ''{0}'' but expected ''{1}''",
                    new String(actualResponse), new String(response)));
        }
    }
}
