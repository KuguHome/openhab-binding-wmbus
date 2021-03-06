/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

import org.openmuc.jmbus.MBusMessage.MessageType;
import org.openmuc.jmbus.VerboseMessage.MessageDirection;
import org.openmuc.jmbus.transportlayer.SerialBuilder;
import org.openmuc.jmbus.transportlayer.TcpBuilder;
import org.openmuc.jmbus.transportlayer.TransportLayer;

/**
 * M-Bus Application Layer connection.
 * <p>
 * Use this access point to communicate using the M-Bus wired protocol.
 * </p>
 * 
 * @see MBusConnection#newSerialBuilder(String)
 * @see MBusConnection#newTcpBuilder(String, int)
 */
public class MBusConnection implements AutoCloseable {

    // 261 is the maximum size of a long frame
    private static final int MAX_MESSAGE_SIZE = 261;

    private final byte[] outputBuffer = new byte[MAX_MESSAGE_SIZE];

    private final byte[] dataRecordsAsBytes = new byte[MAX_MESSAGE_SIZE];

    private final boolean[] frameCountBits;

    private DataOutputStream os;
    private DataInputStream is;

    private SecondaryAddress secondaryAddress;

    private VerboseMessageListener verboseMessageListener;

    private final TransportLayer transportLayer;

    /**
     * Creates an M-Bus Service Access Point that is used to read meters.
     * 
     * @param transportLayer
     *            Underlying transport layer
     * @see MBusConnection#open()
     */
    private MBusConnection(TransportLayer transportLayer) {
        this.transportLayer = transportLayer;

        // set all frame bits to true
        this.frameCountBits = new boolean[254];
        for (int i = 0; i < frameCountBits.length; i++) {
            frameCountBits[i] = true;
        }
    }

    private void open() throws IOException {
        try {
            this.transportLayer.open();
        } catch (IOException e) {
            this.transportLayer.close();
            throw e;
        }

        this.os = transportLayer.getOutputStream();
        this.is = transportLayer.getInputStream();
    }

    /**
     * Closes the service access point.
     */
    @Override
    public void close() {
        transportLayer.close();
    }

    /**
     * Sets the verbose mode on if a implementation of debugMessageListener has been set.
     * 
     * @param verboseMessageListener
     *            Implementation of debugMessageListener
     */
    public void setVerboseMessageListener(VerboseMessageListener verboseMessageListener) {
        this.verboseMessageListener = verboseMessageListener;
    }

    /**
     * Scans for secondary addresses and returns all detected devices in a list and if SecondaryAddressListener not null
     * to the listen listener.
     * 
     * @param wildcardMask
     *            a wildcard mask for masking
     * @param secondaryAddressListener
     *            listener to get scan messages and scanned secondary address just at time.<br>
     *            If null, all detected address will only returned if finished.
     * 
     * @return a list of secondary addresses of all detected devices
     * @throws IOException
     *             if any kind of error (including timeout) occurs while writing to the remote device. Note that the
     *             connection is not closed when an IOException is thrown.
     */
    public List<SecondaryAddress> scan(String wildcardMask, SecondaryAddressListener secondaryAddressListener)
            throws IOException {

        if (wildcardMask == null || wildcardMask.isEmpty()) {
            wildcardMask = "ffffffff";
        }

        return ScanSecondaryAddress.scan(this, wildcardMask, secondaryAddressListener);
    }

    /**
     * Reads a meter using primary addressing. Sends a data request (REQ_UD2) to the remote device and returns the
     * variable data structure from the received RSP_UD frame.
     * 
     * @param primaryAddress
     *            the primary address of the meter to read. For secondary address use 0xfd.
     * @return the variable data structure from the received RSP_UD frame
     * @throws InterruptedIOException
     *             if no response at all (not even a single byte) was received from the meter within the timeout span.
     * @throws IOException
     *             if any kind of error (including timeout) occurs while trying to read the remote device. Note that the
     *             connection is not closed when an IOException is thrown.
     * @throws InterruptedIOException
     *             if no response at all (not even a single byte) was received from the meter within the timeout span.
     */
    public VariableDataStructure read(int primaryAddress) throws IOException, InterruptedIOException {
        if (transportLayer.isClosed()) {
            throw new IllegalStateException("Port is not open.");
        }

        if (frameCountBits[primaryAddress]) {
            sendShortMessage(primaryAddress, 0x7b);
            frameCountBits[primaryAddress] = false;
        } else {
            sendShortMessage(primaryAddress, 0x5b);
            frameCountBits[primaryAddress] = true;
        }

        MBusMessage mBusMessage = receiveMessage();

        if (mBusMessage.getMessageType() != MessageType.RSP_UD) {
            throw new IOException(
                    "Received wrong kind of message. Expected RSP_UD but got: " + mBusMessage.getMessageType());
        }

        if (mBusMessage.getAddressField() != primaryAddress) {
            // throw new IOException("Received RSP_UD message with unexpected address field. Expected " + primaryAddress
            // + " but received " + mBusMessage.getAddressField());
        }

        try {
            mBusMessage.getVariableDataResponse().decode();
        } catch (DecodingException e) {
            throw new IOException("Error decoding incoming RSP_UD message.", e);
        }

        return mBusMessage.getVariableDataResponse();
    }

    /**
     * Sends a long message with individual parameters. Used for messages which arn't not predefined in
     * {@link MBusConnection}.<br>
     * If no response message is expected, set hasResponse to false. Returns <code>null</code> if hasResponse is
     * <code>false</code>
     * 
     * @param primaryAddr
     *            the primary address of the meter to read. For secondary address use 0xfd.
     * @param controlField
     *            control field (C Field) has the size of 1 byte.
     * @param ci
     *            control information field (CI Field) has the size of 1 byte.
     * @param data
     *            the data to sends to the meter.
     * @param responseExpected
     *            set this flag to <code>false</code> if no response is expected else <code>true</code>. If false
     *            returns <code>null</code>
     * @return returns null if <code>boolean hasResponse</code> is <code>false</code>.<br>
     *         returns the {@link MBusMessage} if a message received.
     * @throws IOException
     *             if any kind of error (including timeout) occurs while trying to send to or read the remote device.
     *             Note that the connection is not closed when an IOException is thrown.
     */
    public MBusMessage sendLongMessage(int primaryAddr, int controlField, int ci, byte[] data, boolean responseExpected)
            throws IOException {
        MBusMessage mBusMessage = null;

        sendLongMessage(primaryAddr, controlField, ci, data.length, data);

        if (responseExpected) {
            mBusMessage = receiveMessage();
        }
        return mBusMessage;
    }

    /**
     * Sends a short message with individual parameters. Used for messages which arn't not predefined in
     * {@link MBusConnection}.<br>
     * For normal readout use {@link MBusConnection#read(int)}.<br>
     * If no response message is expected, set hasResponse to false. Returns <code>null</code> if hasResponse is
     * <code>false</code>
     * 
     * @param primaryAddr
     *            the primary address of the meter to read. For secondary address use 0xfd.
     * @param cmd
     *            the command to send to the meter.
     * @param responseExpected
     *            set this flag to <code>false</code> if no response is expected else <code>true</code>. If false
     *            returns <code>null</code>
     * @return returns null if <code>boolean hasResponse</code> is <code>false</code>.<br>
     *         returns the {@link MBusMessage} if a message received.
     * @throws IOException
     *             f any kind of error (including timeout) occurs while trying to send to or read the remote device.
     *             Note that the connection is not closed when an IOException is thrown.
     */
    public MBusMessage sendShortMessage(int primaryAddr, int cmd, boolean responseExpected) throws IOException {
        MBusMessage mBusMessage = null;

        sendShortMessage(primaryAddr, cmd);

        if (responseExpected) {
            mBusMessage = receiveMessage();
        }
        return mBusMessage;
    }

    /**
     * Writes to a meter using primary addressing. Sends a data send (SND_UD) to the remote device and returns a true if
     * slave sends a 0x7e else false
     * 
     * @param primaryAddress
     *            the primary address of the meter to write. For secondary address use 0xfd.
     * @param data
     *            the data to sends to the meter.
     * @throws IOException
     *             if any kind of error (including timeout) occurs while writing to the remote device. Note that the
     *             connection is not closed when an IOException is thrown.
     * @throws InterruptedIOException
     *             if no response at all (not even a single byte) was received from the meter within the timeout span.
     */
    public void write(int primaryAddress, byte[] data) throws IOException, InterruptedIOException {
        if (data == null) {
            data = new byte[0];
        }

        sendLongMessage(primaryAddress, 0x73, 0x51, data.length, data);
        MBusMessage mBusMessage = receiveMessage();

        if (mBusMessage.getMessageType() != MessageType.SINGLE_CHARACTER) {
            throw new IOException("Unable to select component.");
        }
    }

    /**
     * Selects the meter with the specified secondary address. After this the meter can be read on primary address 0xfd.
     * 
     * @param secondaryAddress
     *            the secondary address of the meter to select.
     * @throws IOException
     *             if any kind of error (including timeout) occurs while trying to read the remote device. Note that the
     *             connection is not closed when an IOException is thrown.
     * @throws InterruptedIOException
     *             if no response at all (not even a single byte) was received from the meter within the timeout span.
     */
    public void selectComponent(SecondaryAddress secondaryAddress) throws IOException, InterruptedIOException {
        this.secondaryAddress = secondaryAddress;
        componentSelection(false);
    }

    /**
     * Deselects the previously selected meter.
     * 
     * @throws IOException
     *             if any kind of error (including timeout) occurs while trying to read the remote device. Note that the
     *             connection is not closed when an IOException is thrown.
     * @throws InterruptedIOException
     *             if no response at all (not even a single byte) was received from the meter within the timeout span.
     */
    public void deselectComponent() throws IOException, InterruptedIOException {
        if (secondaryAddress == null) {
            return;
        }
        componentSelection(true);
        secondaryAddress = null;
    }

    /**
     * Selection of wanted records.
     * 
     * @param primaryAddress
     *            primary address of the slave
     * @param dataRecords
     *            data record to select
     * @throws IOException
     *             if any kind of error (including timeout) occurs while trying to read the remote device. Note that the
     *             connection is not closed when an IOException is thrown.
     * @throws InterruptedIOException
     *             if no response at all (not even a single byte) was received from the meter within the timeout span.
     */
    public void selectForReadout(int primaryAddress, List<DataRecord> dataRecords)
            throws IOException, InterruptedIOException {
        int i = 0;
        for (DataRecord dataRecord : dataRecords) {
            i += dataRecord.encode(dataRecordsAsBytes, i);
        }
        sendLongMessage(primaryAddress, 0x53, 0x51, i, dataRecordsAsBytes);
        MBusMessage mBusMessage = receiveMessage();

        if (mBusMessage.getMessageType() != MessageType.SINGLE_CHARACTER) {
            throw new IOException("unable to select component");
        }
    }

    /**
     * Sends a application reset to the slave with specified primary address.
     * 
     * @param primaryAddress
     *            primary address of the slave
     * @throws IOException
     *             if any kind of error (including timeout) occurs while trying to read the remote device. Note that the
     *             connection is not closed when an IOException is thrown.
     * @throws InterruptedIOException
     *             if no response at all (not even a single byte) was received from the meter within the timeout span.
     */
    public void resetReadout(int primaryAddress) throws IOException, InterruptedIOException {
        sendLongMessage(primaryAddress, 0x53, 0x50, 0, new byte[] {});
        MBusMessage mBusMessage = receiveMessage();

        if (mBusMessage.getMessageType() != MessageType.SINGLE_CHARACTER) {
            throw new IOException("Unable to reset application.");
        }
    }

    /**
     * Sends a SND_NKE message to reset the FCB (frame counter bit).
     * 
     * @param primaryAddress
     *            the primary address of the meter to reset.
     * @throws InterruptedIOException
     *             if the slave does not answer with an 0xe5 message within the configured timeout span.
     * @throws IOException
     *             if an error occurs during the reset process.
     * @throws InterruptedIOException
     *             if the slave does not answer with an 0xe5 message within the configured timeout span.
     */
    public void linkReset(int primaryAddress) throws IOException, InterruptedIOException {
        sendShortMessage(primaryAddress, 0x40);
        MBusMessage mBusMessage = receiveMessage();

        if (mBusMessage.getMessageType() != MessageType.SINGLE_CHARACTER) {
            throw new IOException("Unable to reset link.");
        }

        frameCountBits[primaryAddress] = true;
    }

    private void componentSelection(boolean deselect) throws IOException, InterruptedIOException {
        byte[] ba = secondaryAddressAsBa();

        // send select/deselect
        if (deselect) {
            sendLongMessage(0xfd, 0x53, 0x56, 8, ba);
        } else {
            sendLongMessage(0xfd, 0x53, 0x52, 8, ba);
        }

        MBusMessage mBusMessage = receiveMessage();

        if (mBusMessage.getMessageType() != MessageType.SINGLE_CHARACTER) {
            throw new IOException("unable to select component");
        }
    }

    private byte[] secondaryAddressAsBa() {
        byte[] ba = new byte[8];

        ((ByteBuffer) ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(secondaryAddress.asByteArray())
                .position(0)).get(ba, 0, 8);
        return ba;
    }

    private void sendShortMessage(int slaveAddr, int cmd) throws IOException {
        synchronized (os) {
            outputBuffer[0] = 0x10;
            outputBuffer[1] = (byte) (cmd);
            outputBuffer[2] = (byte) (slaveAddr);
            outputBuffer[3] = (byte) (cmd + slaveAddr);
            outputBuffer[4] = 0x16;

            verboseMessage(MessageDirection.SEND, outputBuffer, 0, 5);

            os.write(outputBuffer, 0, 5);
        }
    }

    void sendLongMessage(int slaveAddr, int controlField, int ci, int length, byte[] data) throws IOException {
        synchronized (os) {
            outputBuffer[0] = 0x68;
            outputBuffer[1] = (byte) (length + 3);
            outputBuffer[2] = (byte) (length + 3);
            outputBuffer[3] = 0x68;
            outputBuffer[4] = (byte) controlField;
            outputBuffer[5] = (byte) slaveAddr;
            outputBuffer[6] = (byte) ci;

            for (int i = 0; i < length; i++) {
                outputBuffer[7 + i] = data[i];
            }

            outputBuffer[length + 7] = computeChecksum(length, outputBuffer);

            outputBuffer[length + 8] = 0x16;

            verboseMessage(MessageDirection.SEND, outputBuffer, 0, length + 9);

            os.write(outputBuffer, 0, length + 9);
        }
    }

    private static byte computeChecksum(int length, byte[] oBuffer) {
        int checksum = 0;
        for (int j = 4; j < (length + 7); j++) {
            checksum += oBuffer[j];
        }
        return (byte) (checksum & 0xff);
    }

    MBusMessage receiveMessage() throws IOException {
        byte[] receivedBytes;

        int b0 = is.read();
        if (b0 == 0xe5) {
            // messageLength = 1;
            receivedBytes = new byte[] { (byte) b0 };
        } else if ((b0 & 0xff) == 0x68) {
            int b1 = is.readByte() & 0xff;

            /**
             * The L field gives the quantity of the user data inputs plus 3 (for C,A,CI).
             */
            int messageLength = b1 + 6;

            receivedBytes = new byte[messageLength];
            receivedBytes[0] = (byte) b0;
            receivedBytes[1] = (byte) b1;

            int lenRead = messageLength - 2;

            is.readFully(receivedBytes, 2, lenRead);
        } else {
            throw new IOException(String.format("Received unknown message: %02X", b0));
        }

        verboseMessage(MessageDirection.RECEIVE, receivedBytes, 0, receivedBytes.length);

        return MBusMessage.decode(receivedBytes, receivedBytes.length);
    }

    private void verboseMessage(MessageDirection direction, byte[] array, int from, int to) {
        if (this.verboseMessageListener != null) {
            byte[] message = Arrays.copyOfRange(array, from, to);

            VerboseMessage debugMessage = new VerboseMessage(direction, message);
            this.verboseMessageListener.newVerboseMessage(debugMessage);
        }
    }

    public static MBusTcpBuilder newTcpBuilder(String hostAddress, int port) {
        return new MBusTcpBuilder(hostAddress, port);
    }

    public static class MBusTcpBuilder extends TcpBuilder<MBusConnection, MBusTcpBuilder> {

        protected MBusTcpBuilder(String hostAddress, int port) {
            super(hostAddress, port);
        }

        @Override
        public MBusConnection build() throws IOException {
            MBusConnection mBusConnection = new MBusConnection(buildTransportLayer());
            mBusConnection.open();
            return mBusConnection;
        }
    }

    /**
     * Create a new builder to connect to a serial.
     * 
     * @param serialPortName
     *            the serial port. e.g. <code>/dev/ttyS0</code>.
     * @return a new connection builder.
     */
    public static MBusSerialBuilder newSerialBuilder(String serialPortName) {
        return new MBusSerialBuilder(serialPortName);
    }

    public static class MBusSerialBuilder extends SerialBuilder<MBusConnection, MBusSerialBuilder> {

        protected MBusSerialBuilder(String serialPortName) {
            super(serialPortName);
        }

        @Override
        public MBusConnection build() throws IOException {
            MBusConnection mBusConnection = new MBusConnection(buildTransportLayer());
            mBusConnection.open();
            return mBusConnection;
        }
    }
}
