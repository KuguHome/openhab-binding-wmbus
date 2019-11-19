/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.internal.WMBusReceiver;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.VirtualWMBusMessageHelper;
import org.openmuc.jmbus.wireless.WMBusConnection;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusManufacturer;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusSerialBuilder;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.openmuc.jmbus.wireless.WMBusMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WMBusBridgeHandler} class defines This class represents the WMBus bridge and handles general events for
 * the whole group of WMBus devices.
 *
 * @author Hanno - Felix Wagner - Initial contribution
 */

public class WMBusBridgeHandler extends ConfigStatusBridgeHandler implements WMBusAdapter {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private static final ScheduledExecutorService SCHEDULER = ThreadPoolManager.getScheduledPool("wmbus");

    private static final String DEVICE_STATE_ADDED = "added";

    private static final String DEVICE_STATE_CHANGED = "changed";

    private final Logger logger = LoggerFactory.getLogger(WMBusBridgeHandler.class);

    private final KeyStorage keyStorage;

    protected WMBusReceiver wmbusReceiver;
    private WMBusConnection wmbusConnection;
    private ScheduledFuture<?> initFuture;

    private final Map<String, WMBusDevice> knownDevices = new ConcurrentHashMap<>();
    private final Set<WMBusDeviceHandler<WMBusDevice>> handlers = Collections.synchronizedSet(new HashSet<>());

    private final List<WMBusMessageListener> wmBusMessageListeners = new CopyOnWriteArrayList<>();

    private ScheduledFuture<?> statusFuture;

    public WMBusBridgeHandler(Bridge bridge, KeyStorage keyStorage) {
        super(bridge);
        this.keyStorage = keyStorage;
        this.statusFuture = SCHEDULER.scheduleAtFixedRate(new StatusRunnable(handlers), 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        return Collections.emptyList(); // all good, otherwise add some messages
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // judging from the hue bridge, this seems to be not needed...?
        logger.debug("WARNING: Unexpected call of handleCommand(). Parameters are channelUID={} and command={}",
                channelUID, command);
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

    public boolean registerWMBusMessageListener(WMBusMessageListener wmBusMessageListener) {
        if (wmBusMessageListener == null) {
            return false;
        }

        logger.debug("register listener: Adding");
        boolean result = wmBusMessageListeners.add(wmBusMessageListener);
        logger.debug("register listener: Success");
        if (result) {
            // inform the listener initially about all devices and their states
            for (WMBusDevice device : knownDevices.values()) {
                wmBusMessageListener.onNewWMBusDevice(this, device);
            }
        }
        return result;
    }

    /**
     * Iterate through wmBusMessageListeners and notify them about a newly received message.
     *
     * @param device
     */
    private void notifyWMBusMessageListeners(final WMBusDevice device, final String type) {
        logger.trace("bridge: notify message listeners: sending to all");
        for (WMBusMessageListener wmBusMessageListener : wmBusMessageListeners) {
            try {
                switch (type) {
                    case DEVICE_STATE_ADDED: {
                        wmBusMessageListener.onNewWMBusDevice(this, decrypt(device));
                        break;
                    }
                    case DEVICE_STATE_CHANGED: {
                        wmBusMessageListener.onChangedWMBusDevice(this, decrypt(device));
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException(
                                "Could not notify wmBusMessageListeners for unknown event type " + type);
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while notifying the WMBusMessageListener", e);
            }
        }
        logger.trace("bridge: notify message listeners: return");
    }

    @Override
    public void dispose() {
        logger.debug("WMBus bridge Handler disposed.");

        if (initFuture != null && !initFuture.isDone()) {
            initFuture.cancel(true);
        }
        initFuture = null;

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

        if (statusFuture != null) {
            statusFuture.cancel(true);
            statusFuture = null;
        }
    }

    /**
     * Because we do not add encryption keys to connection and they are propagated from connection down to received
     * frame and its parsing logic we need to inject encryption keys after message is received and before its first use
     * to avoid troubles.
     * Yes, we do it manually because jmbus does not offer any API/SPI for that.
     *
     * @param device Incoming frame.
     * @return Decrypted frame or original (unencrypted) frame when parsing fails.
     */
    protected WMBusDevice decrypt(WMBusDevice device) {
        try {
            device.decode();
        } catch (DecodingException parseException) {
            if (parseException.getMessage().startsWith("Unable to decode encrypted payload")) {
                try {
                    WMBusMessage message = VirtualWMBusMessageHelper.decode(device.getOriginalMessage().asBlob(),
                            device.getOriginalMessage().getRssi(), keyStorage.toMap());
                    logger.info("Message from {} successfully deecrypted, forwarding it to receivers",
                            device.getDeviceAddress());
                    message.getVariableDataResponse().decode();
                    return new WMBusDevice(message, this);
                } catch (DecodingException decodingException) {
                    logger.info(
                            "Could not decode frame, probably we still miss enryption key, forwarding frame in original form");
                }
            } else if (parseException.getMessage().startsWith("Manufacturer specific CI:")) {
                logger.trace("Found frame with manufacturer specific encoding, forwarding for futher processing.");
            } else {
                logger.debug("Unexpected error while parsing frame, forwarding frame in original form", parseException);
            }
        }
        return device;
    }

    @Override
    public void processMessage(WMBusDevice device) {
        logger.trace("bridge: processMessage begin");

        String deviceAddress = device.getDeviceAddress();
        String deviceState = DEVICE_STATE_ADDED;
        if (knownDevices.containsKey(deviceAddress)) {
            deviceState = DEVICE_STATE_CHANGED;
        }
        knownDevices.put(deviceAddress, device);
        logger.trace("bridge processMessage: notifying listeners");
        notifyWMBusMessageListeners(device, deviceState);
        logger.trace("bridge: processMessage end");
    }

    public WMBusDevice getDeviceByAddress(String deviceAddress) {
        logger.trace("bridge: get device by address: " + deviceAddress);
        if (knownDevices.containsKey(deviceAddress)) {
            logger.trace("bridge: found device");
        } else {
            logger.trace("bridge: device not found");
        }
        return knownDevices.get(deviceAddress);
    }

    private int[] parseDeviceIDFilter() {
        String[] ids = ((String) getConfig().get(WMBusBindingConstants.CONFKEY_DEVICEID_FILTER)).split(";");
        int[] idInts = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            String curID = ids[i];
            idInts[i] = Integer.valueOf(curID);
        }
        return idInts;
    }

    @Override
    public ThingUID getUID() {
        return getThing().getUID();
    }

    @Override
    public void childHandlerInitialized(@NonNull ThingHandler childHandler, @NonNull Thing childThing) {
        if (childHandler instanceof WMBusDeviceHandler) {
            handlers.add((WMBusDeviceHandler<WMBusDevice>) childHandler);
        }
    }

    @Override
    public void childHandlerDisposed(@NonNull ThingHandler childHandler, @NonNull Thing childThing) {
        if (childHandler instanceof WMBusDeviceHandler) {
            handlers.remove(childHandler);
        }
    }

    static class StatusRunnable implements Runnable {

        private final Set<WMBusDeviceHandler<WMBusDevice>> handlers;

        public StatusRunnable(Set<WMBusDeviceHandler<WMBusDevice>> handlers) {
            this.handlers = handlers;
        }

        @Override
        public void run() {
            handlers.stream().forEach(WMBusDeviceHandler::checkStatus);
        }

    }
}
