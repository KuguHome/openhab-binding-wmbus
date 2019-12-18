/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.handler;

import static org.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.internal.WMBusReceiver;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.openmuc.jmbus.wireless.WMBusConnection;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusManufacturer;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusSerialBuilder;
import org.openmuc.jmbus.wireless.WMBusMode;

/**
 * The {@link WMBusBridgeHandler} class defines This class represents the WMBus bridge and handles general events for
 * the whole group of WMBus devices.
 *
 * @author Hanno - Felix Wagner - Initial contribution
 */

public class WMBusBridgeHandler extends WMBusBridgeHandlerBase {

    private ScheduledFuture<?> initFuture;
    private WMBusConnection wmbusConnection;

    public WMBusBridgeHandler(Bridge bridge, KeyStorage keyStorage) {
        super(bridge, keyStorage);
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        return Collections.emptyList(); // all good, otherwise add some messages
    }

    /**
     * Connects to the WMBus radio module and updates bridge status.
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
     */
    @Override
    public void initialize() {
        logger.debug("WMBusBridgeHandler: initialize()");

        // check stick model
        if (!getConfig().containsKey(WMBusBindingConstants.CONFKEY_STICK_MODEL)
                || getConfig().get(WMBusBindingConstants.CONFKEY_STICK_MODEL) == null
                || ((String) getConfig().get(WMBusBindingConstants.CONFKEY_STICK_MODEL)).isEmpty()) {
            logger.error("Cannot open WMBus device. Stick model not given.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot open WMBus device. Stick model not given.");
            return;
        }

        // check serial device name
        if (!getConfig().containsKey(WMBusBindingConstants.CONFKEY_INTERFACE_NAME)
                || getConfig().get(WMBusBindingConstants.CONFKEY_INTERFACE_NAME) == null
                || ((String) getConfig().get(WMBusBindingConstants.CONFKEY_RADIO_MODE)).isEmpty()) {
            logger.error("Cannot open WMBus device. Serial device name not given.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot open WMBus device. Serial device name not given.");
            return;
        }

        // check radio mode
        if (!getConfig().containsKey(WMBusBindingConstants.CONFKEY_RADIO_MODE)
                || getConfig().get(WMBusBindingConstants.CONFKEY_RADIO_MODE) == null
                || ((String) getConfig().get(WMBusBindingConstants.CONFKEY_RADIO_MODE)).isEmpty()) {
            logger.error("Cannot open WMBus device. Radio mode not given.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot open WMBus device. Radio mode not given.");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        initFuture = scheduler.schedule(() -> {
            // set up WMBus receiver = handler for radio telegrams
            if (wmbusReceiver == null) {
                String stickModel = (String) getConfig().get(WMBusBindingConstants.CONFKEY_STICK_MODEL);
                String interfaceName = (String) getConfig().get(WMBusBindingConstants.CONFKEY_INTERFACE_NAME);
                String radioModeStr = (String) getConfig().get(WMBusBindingConstants.CONFKEY_RADIO_MODE);

                // connect to the radio module / open WMBus connection
                logger.debug("Opening wmbus stick {} serial port {} in mode {}", stickModel, interfaceName,
                        radioModeStr);

                WMBusManufacturer wmBusManufacturer = parseManufacturer(stickModel);
                if (wmBusManufacturer == null) {
                    logger.error("Cannot open WMBus device. Unknown manufacturer given: " + stickModel
                            + ". Expected 'amber' or 'imst' or 'rc'.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "Cannot open WMBus device. Unknown manufacturer given: " + stickModel
                                    + ". Expected 'amber' or 'imst' or 'rc'.");
                    return;
                }
                logger.debug("Building new connection");

                wmbusReceiver = new WMBusReceiver(this);

                if (!getConfig().containsKey(WMBusBindingConstants.CONFKEY_DEVICEID_FILTER)
                        || getConfig().get(WMBusBindingConstants.CONFKEY_DEVICEID_FILTER) == null
                        || ((String) getConfig().get(WMBusBindingConstants.CONFKEY_DEVICEID_FILTER)).isEmpty()) {
                    logger.debug("Device ID filter is empty.");
                } else {
                    wmbusReceiver.setFilterIDs(parseDeviceIDFilter());
                }

                WMBusSerialBuilder connectionBuilder = new WMBusSerialBuilder(wmBusManufacturer, wmbusReceiver,
                        interfaceName);

                WMBusMode radioMode = null;
                try {
                    radioMode = WMBusMode.valueOf(radioModeStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.error("Cannot open WMBus device. Unknown radio mode given: " + radioModeStr
                            + ". Expected 'S', 'T', or 'C'.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "Cannot open WMBus device. Unknown radio mode given: " + radioModeStr
                                    + ". Expected 'S', 'T', or 'C'.");
                    return;
                }

                logger.debug("Setting WMBus radio mode to {}", radioMode.toString());
                connectionBuilder.setMode(radioMode);
                connectionBuilder.setTimeout(1000); // infinite
                // connectionBuilder.setTimeout(0); // infinite

                try {
                    logger.debug("Building/opening connection");
                    logger.debug(
                            "NOTE: if initialization does not progress from here, check systemd journal for Execptions -- probably native lib still loaded by another ClassLoader = previous version or instance of WMBus binding -> restart OpenHAB");
                    if (wmbusConnection != null) {
                        logger.debug("Connection already set, closing old");
                        wmbusConnection.close();
                        wmbusConnection = null;
                    }
                    wmbusConnection = connectionBuilder.build();
                } catch (IOException e) {
                    logger.error("Cannot open WMBus device. Connection builder returned: " + e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "Cannot open WMBus device. Connection builder returned: " + e.getMessage());
                    return;
                }

                logger.debug("Connected to WMBus serial port");

                // close WMBus connection on shutdown
                logger.trace("Setting shutdown hook");
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        if (wmbusConnection != null) {
                            try {
                                logger.debug("Closing connection to WMBus radio device");
                                wmbusConnection.close();
                            } catch (IOException e) {
                                logger.error("Cannot close connection to WMBus radio module: " + e.getMessage());
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                                        "Cannot close connection to WMBus radio module: " + e.getMessage());
                                return;
                            }
                        }
                    }
                });

                // success
                logger.debug("WMBusBridgeHandler: Initialization done! Setting bridge online");
                updateStatus(ThingStatus.ONLINE);
            }
        }, 0, TimeUnit.SECONDS);
    }

    private static WMBusManufacturer parseManufacturer(String manufacturer) {
        switch (manufacturer.toLowerCase()) {
            case MANUFACTURER_AMBER:
                return WMBusManufacturer.AMBER;
            case MANUFACTURER_RADIO_CRAFTS:
                return WMBusManufacturer.RADIO_CRAFTS;
            case MANUFACTURER_IMST:
                return WMBusManufacturer.IMST;
            default:
                return null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        logger.debug("WMBus bridge Handler disposed.");

        if (wmbusConnection != null) {
            logger.debug("Close serial device connection");
            try {
                wmbusConnection.close();
            } catch (IOException e) {
                logger.error("An exception occurred while closing the wmbusConnection", e);
            }
            wmbusConnection = null;
        }

        if (wmbusReceiver != null) {
            wmbusReceiver = null;
        }

        if (initFuture != null) {
            initFuture.cancel(true);
            initFuture = null;
        }
    }

}
