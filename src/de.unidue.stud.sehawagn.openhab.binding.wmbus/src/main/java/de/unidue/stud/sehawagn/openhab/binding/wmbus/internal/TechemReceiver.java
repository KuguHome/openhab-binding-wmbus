/*
 * Copyright 2010-16 Fraunhofer ISE
 *
 * This file is part of jMBus.
 * For more information visit http://www.openmuc.org
 *
 * jMBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jMBus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jMBus.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
 * Keeps connection with the WMBus radio module and forwards TODO Javadoc class description
 *
 * TODO generalize to be responsible not only for Techem HKV messages, but be the general WMBus message receiver.
 *
 * @author
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

    @Override
    /*
     * Handle incoming WMBus message from radio module.
     *
     * @see org.openmuc.jmbus.WMBusListener#newMessage(org.openmuc.jmbus.WMBusMessage)
     */
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