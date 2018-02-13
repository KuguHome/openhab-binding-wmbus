package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal;

import java.io.IOException;

//import org.openmuc.jmbus.HexConverter;
//import org.openmuc.jmbus.TechemHKVMessage;
import org.openmuc.jmbus.wireless.WMBusListener;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusBridgeHandler;

/**
 * Keeps connection with WMBus radio module and forwards received messages to WMBusBridgeHandler for processing
 *
 * TODO Javadoc class description
 * TODO generalize to be responsible not only for Techem HKV messages, but be the general WMBus message receiver.
 *
 */
public class TechemReceiver implements WMBusListener {

    int[] filterIDs = new int[] {};

    private WMBusBridgeHandler wmBusBridgeHandler;

    public static String VENDOR_TECHEM = "TCH";

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(TechemReceiver.class);

    public TechemReceiver(WMBusBridgeHandler wmBusBridgeHandler) {
        // TODO nur die verallgemeinerte Version WMBusReceiver.
        logger.debug("ERROR: TechemReceiver should not be used ATM");
        this.wmBusBridgeHandler = wmBusBridgeHandler;
    }

    public int[] getFilterIDs() {
        return filterIDs;
    }

    public void setFilterIDs(int[] filterIDs) {
        this.filterIDs = filterIDs;
    }

    // TODO
    /*
     * public void init(String serialPortName, String receiverManufacturer, WMBusMode radioMode) throws IOException {
     *
     * // open WMBus connection
     * // TODO support the two other radio modules as well (RadioCrafts in v2.2; IMST stick supported since jMBus v3.0)
     * logger.debug("Opening wmbus serial port {} in mode {}", serialPortName, radioMode.toString());
     *
     * WMBusManufacturer wmBusManufacturer = parseManufacturer(receiverManufacturer);
     *
     * WMBusSerialBuilder connectionBuilder = new WMBusSerialBuilder(wmBusManufacturer,
     * new WMBusStart.WMBusReceiver(this.cliPrinter), serialPortName);
     * connectionBuilder.setMode(radioMode);
     *
     * WMBusConnection wmBusConnection = (WMBusConnection) builder.build();
     * //Map<SecondaryAddress, byte[]> keyPairs = getKeyPairs();
     * //for (Entry<SecondaryAddress, byte[]> keyPair : keyPairs.entrySet()) {
     * //wmBusConnection.addKey(keyPair.getKey(), keyPair.getValue());
     * //}
     *
     * final WMBusSap wMBusSap = new WMBusSapAmber(serialPortName, radioMode, this);
     * wMBusSap.open(); // this can throw the IOException - will be caught in WMBusBridgeHandler.initialize()
     * logger.debug("Connected to WMBus serial port");
     *
     * // close WMBus connection on shutdown
     * Runtime.getRuntime().addShutdownHook(new Thread() {
     *
     * @Override
     * public void run() {
     * if (wMBusSap != null) {
     * wMBusSap.close();
     * }
     * }
     * });
     *
     * }
     */

    boolean filterMatch(int inQuestion) {
        if (filterIDs.length == 0) {
            return true;
        }
        for (int i = 0; i < filterIDs.length; i++) {
            if (filterIDs[i] == inQuestion) {
                return true;
            }
        }
        return false;
    }

    /*
     * Handle incoming WMBus message from radio module.
     *
     * @see org.openmuc.jmbus.WMBusListener#newMessage(org.openmuc.jmbus.WMBusMessage)
     */
    @Override
    public void newMessage(WMBusMessage message) {
        // try {
        // TODO
        // message.decodeDeep();
        if (filterMatch(message.getSecondaryAddress().getDeviceId().intValue())) {
            logger.debug("Matched message received: " + message.toString());
            wmBusBridgeHandler.processMessage(message);
        } else {
            logger.debug("Unmatched message received: " + message.toString());
        }
        // } catch (DecodingException e) {
        // try harder to decode
        // TODO what is happening here?
        /*
         * byte[] messageBytes = message.asBytes();
         * if ((messageBytes.length == 51 || messageBytes.length == 47) && (messageBytes[10] & 0xff) == 0xa0
         * && message.getSecondaryAddress().getManufacturerId().equals(VENDOR_TECHEM)) {
         * newMessage(new TechemHKVMessage(message)); // standard a0
         * } else if ((messageBytes[10] & 0xff) == 0xa2
         * && message.getSecondaryAddress().getManufacturerId().equals("TCH")) {
         * newMessage(new TechemHKVMessage(message)); // at Karl's
         * } else if ((messageBytes[10] & 0xff) == 0x80
         * && message.getSecondaryAddress().getManufacturerId().equals("TCH")) {
         * newMessage(new TechemHKVMessage(message)); // at Karl's - warmwater?
         * } else {
         * // still could not decode message
         * logger.debug("TechemReceiver: Unable to fully decode received message: " + e.getMessage());
         * logger.debug("messageBytes.length=" + messageBytes.length + " (messageBytes[10] & 0xff)="
         * + (messageBytes[10] & 0xff) + " message.getSecondaryAddress().getManufacturerId()="
         * + message.getSecondaryAddress().getManufacturerId());
         * logger.debug("Message: " + message.toString());
         * logger.debug("Stack trace: " + e.getStackTrace().toString());
         * }
         */
        // }
    }

    @Override
    public void discardedBytes(byte[] bytes) {
        logger.debug("Bytes discarded by radio module: " + HexConverter.bytesToHex(bytes));
    }

    @Override
    public void stoppedListening(IOException e) {
        logger.debug("Stopped listening for new messages. Reason: {}", e.getMessage());
    }
}
