/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.wmbus.internal;

import java.io.IOException;
import java.util.Arrays;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.core.util.HexUtils;
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

    private final WMBusAdapter wmBusBridgeHandler;

    private final Logger logger = LoggerFactory.getLogger(WMBusReceiver.class);

    public WMBusReceiver(WMBusAdapter wmBusBridgeHandler) {
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
        wmBusBridgeHandler.reset();
        logger.warn("Stopped listening for new messages. Reason: {}", e);
    }
}
