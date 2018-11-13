/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.internal;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusBridgeHandler;
import org.openmuc.jmbus.wireless.WMBusListener;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WMBusReceiver} class defines WMBusReceiver. Keeps connection with the WMBus radio module and forwards
 *
 * @author Hanno - Felix Wagner - Roman Malyugin - Initial contribution
 */

public class WMBusReceiver implements WMBusListener {

    int[] filterIDs = new int[] {};

    private final WMBusBridgeHandler wmBusBridgeHandler;

    private final Logger logger = LoggerFactory.getLogger(WMBusReceiver.class);

    public WMBusReceiver(WMBusBridgeHandler wmBusBridgeHandler) {
        this.wmBusBridgeHandler = wmBusBridgeHandler;
    }

    public int[] getFilterIDs() {
        logger.debug("getFilterIDs(): got {}", Arrays.toString(filterIDs));
        return filterIDs;
    }

    public void setFilterIDs(int[] filterIDs) {
        logger.debug("setFilterIDs() to {}", Arrays.toString(filterIDs));
        this.filterIDs = filterIDs;
    }

    boolean filterMatch(int inQuestion) {
        logger.trace("filterMatch(): are we interested in device {}?", Integer.toString(inQuestion));
        if (filterIDs.length == 0) {
            logger.trace("filterMatch(): length is zero -> yes");
            return true;
        }
        for (int i = 0; i < filterIDs.length; i++) {
            if (filterIDs[i] == inQuestion) {
                logger.debug("filterMatch(): found the device -> yes");
                return true;
            }
        }
        logger.trace("filterMatch(): not found");
        return false;
    }

    /*
     * Handle incoming WMBus message from radio module.
     *
     * @see org.openmuc.jmbus.WMBusListener#newMessage(org.openmuc.jmbus.WMBusMessage)
     */
    @Override
    public void newMessage(WMBusMessage message) {
        logger.trace("Received WMBus message");
        if (filterMatch(message.getSecondaryAddress().getDeviceId().intValue())) {
            WMBusDevice device = new WMBusDevice(message, wmBusBridgeHandler);

            logger.trace("Forwarding message to further processing: {}", HexUtils.bytesToHex(message.asBlob()));
            wmBusBridgeHandler.processMessage(device);
        } else {
            logger.trace("Unmatched message received: {}", HexUtils.bytesToHex(message.asBlob()));
        }
    }

    @Override
    public void discardedBytes(byte[] bytes) {
        logger.debug("Bytes discarded by radio module: {}", HexConverter.bytesToHex(bytes));
    }

    @Override
    public void stoppedListening(IOException e) {
        logger.warn("Stopped listening for new messages. Reason: {}", e);
    }

}