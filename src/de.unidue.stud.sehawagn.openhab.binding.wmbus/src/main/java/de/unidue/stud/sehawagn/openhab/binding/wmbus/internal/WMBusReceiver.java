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
import java.util.Arrays;

import org.openmuc.jmbus.DecodingException;
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
public class WMBusReceiver implements WMBusListener {

    int[] filterIDs = new int[] {};

    private WMBusBridgeHandler wmBusBridgeHandler;

    public static String VENDOR_TECHEM = "TCH";
    public static String VENDOR_QUNDIS = "QDS";
    public static String VENDOR_KAMSTRUP = "KAM";

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(WMBusReceiver.class);

    public WMBusReceiver(WMBusBridgeHandler wmBusBridgeHandler) {
        this.wmBusBridgeHandler = wmBusBridgeHandler;
    }

    public int[] getFilterIDs() {
        logger.debug("receiver: get filter IDs: got " + Arrays.toString(filterIDs));
        return filterIDs;
    }

    public void setFilterIDs(int[] filterIDs) {
        logger.debug("receiver: setFilterIDs to " + Arrays.toString(filterIDs));
        this.filterIDs = filterIDs;
    }

    boolean filterMatch(int inQuestion) {
        logger.debug("receiver: filterMatch(): do we know device: " + Integer.toString(inQuestion));
        if (filterIDs.length == 0) {
            logger.debug("receiver: filterMatch(): length is zero -> yes");
            return true;
        }
        for (int i = 0; i < filterIDs.length; i++) {
            if (filterIDs[i] == inQuestion) {
                logger.debug("receiver: filterMatch(): found the device");
                return true;
            }
        }
        logger.debug("receiver: filterMatch(): not found");
        return false;
    }

    @Override
    /*
     * Handle incoming WMBus message from radio module.
     *
     * @see org.openmuc.jmbus.WMBusListener#newMessage(org.openmuc.jmbus.WMBusMessage)
     */
    public void newMessage(WMBusMessage message) {
        WMBusDevice device = null;
        if (filterMatch(message.getSecondaryAddress().getDeviceId().intValue())) {
            // decode VDR
            try {
                message.getVariableDataResponse().decode();
                message.getVariableDataResponse().getDataRecords();

                logger.debug("receiver: variable data response decoded");
            } catch (DecodingException e) {
                logger.debug("receiver: could not decode variable data response: " + e.getMessage());
                device = new TechemHKV(message);
                try {
                    logger.debug("receiver: try to decode as Techem Message:");
                    device.decode();
                    wmBusBridgeHandler.processMessage(device);
                } catch (DecodingException e1) {
                    logger.debug("receiver: could not decode as Techem Message: " + e1.getMessage());
                    e1.printStackTrace();
                }
            }
            // print it
            logger.debug("receiver: Matched message received: " + message.toString());
            ///
            // get variable response, decrypt.
            // getdatarecords - DIB und VIB
            wmBusBridgeHandler.processMessage(device);
            logger.debug("receiver: Forwarded to handler.processMessage()");
        } else {
            logger.debug("receiver: Unmatched message received: " + message.toString());
        }
    }

    @Override
    public void discardedBytes(byte[] bytes) {
        logger.debug("receiver: Bytes discarded by radio module: " + HexConverter.bytesToHex(bytes));
    }

    @Override
    public void stoppedListening(IOException e) {
        logger.debug("receiver: Stopped listening for new messages. Reason: {}", e.getMessage());
    }

}