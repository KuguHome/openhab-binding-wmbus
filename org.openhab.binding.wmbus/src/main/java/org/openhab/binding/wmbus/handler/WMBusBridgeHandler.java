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

package org.openhab.binding.wmbus.handler;

import static org.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.wmbus.config.StickModel;
import org.openhab.binding.wmbus.config.WMBusSerialBridgeConfig;
import org.openhab.binding.wmbus.internal.WMBusReceiver;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
        List<ConfigStatusMessage> messages = new ArrayList<>();
        WMBusSerialBridgeConfig config = getConfigAs(WMBusSerialBridgeConfig.class);

        // check stick model
        if (config.stickModel == null) {
            messages.add(ConfigStatusMessage.Builder.error(CONFKEY_STICK_MODEL)
                    .withMessageKeySuffix(CONFKEY_STICK_MODEL).build());
        }
        // check serial device name
        if (config.serialDevice == null) {
            messages.add(ConfigStatusMessage.Builder.error(CONFKEY_INTERFACE_NAME)
                    .withMessageKeySuffix(CONFKEY_INTERFACE_NAME).build());
        }
        // check radio mode
        if (config.radioMode == null) {
            messages.add(ConfigStatusMessage.Builder.error(CONFKEY_RADIO_MODE).withMessageKeySuffix(CONFKEY_RADIO_MODE)
                    .build());
        }

        return messages;
    }

    /**
     * Connects to the WMBus radio module and updates bridge status.
     *
     * @see org.openhab.core.thing.binding.BaseThingHandler#initialize()
     */
    @Override
    public void initialize() {
        logger.debug("WMBusBridgeHandler: initialize()");

        WMBusSerialBridgeConfig config = getConfigAs(WMBusSerialBridgeConfig.class);
        updateStatus(ThingStatus.UNKNOWN);
        initFuture = scheduler.schedule(() -> {
            // set up WMBus receiver = handler for radio telegrams
            if (wmbusReceiver == null) {
                StickModel stickModel = config.stickModel;
                String interfaceName = config.serialDevice;
                WMBusMode radioMode = config.radioMode;

                // connect to the radio module / open WMBus connection
                logger.debug("Opening wmbus stick {} serial port {} in mode {}", stickModel, interfaceName, radioMode);

                WMBusManufacturer wmBusManufacturer = parseManufacturer(stickModel.name().toUpperCase());
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

                if (config.deviceIDFilter == null || config.deviceIDFilter.trim().isEmpty()) {
                    logger.debug("Device ID filter is empty.");
                } else {
                    wmbusReceiver.setFilterIDs(config.getDeviceIDFilter());
                }

                WMBusSerialBuilder connectionBuilder = new WMBusSerialBuilder(wmBusManufacturer, wmbusReceiver,
                        interfaceName);

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
            case MANUFACTURER_CUL:
                return WMBusManufacturer.CUL;
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
