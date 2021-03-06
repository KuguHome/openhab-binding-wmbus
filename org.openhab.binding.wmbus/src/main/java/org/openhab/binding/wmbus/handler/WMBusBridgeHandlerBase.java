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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.config.DateFieldMode;
import org.openhab.binding.wmbus.config.WMBusBridgeConfig;
import org.openhab.binding.wmbus.internal.WMBusReceiver;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.VirtualWMBusMessageHelper;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WMBusBridgeHandlerBase} class defines base operations which are common for all bridge
 * handlers such as device and key association.
 *
 * @author Łukasz Dywicki - Initial contribution, extracted from {@link WMBusBridgeHandler}.
 */
public abstract class WMBusBridgeHandlerBase extends ConfigStatusBridgeHandler implements WMBusAdapter {

    private static final ScheduledExecutorService SCHEDULER = ThreadPoolManager.getScheduledPool("wmbus");

    private static final String DEVICE_STATE_ADDED = "added";
    private static final String DEVICE_STATE_CHANGED = "changed";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final KeyStorage keyStorage;
    private final Map<String, WMBusDevice> knownDevices = new ConcurrentHashMap<>();
    private final Set<WMBusDeviceHandler<WMBusDevice>> handlers = Collections.synchronizedSet(new HashSet<>());
    private final List<WMBusMessageListener> wmBusMessageListeners = new CopyOnWriteArrayList<>();
    protected WMBusReceiver wmbusReceiver;
    private ScheduledFuture<?> statusFuture;
    private AtomicBoolean updateFrames = new AtomicBoolean(false);

    public WMBusBridgeHandlerBase(Bridge bridge, KeyStorage keyStorage) {
        super(bridge);
        this.keyStorage = keyStorage;
        this.statusFuture = SCHEDULER.scheduleAtFixedRate(new StatusRunnable(handlers), 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // judging from the hue bridge, this seems to be not needed...?
        logger.warn("Unexpected command for bridge. Parameters are channelUID={} and command={}", channelUID, command);
    }

    @Override
    public void dispose() {
        if (statusFuture != null) {
            statusFuture.cancel(true);
            statusFuture = null;
        }
    }

    public boolean registerWMBusMessageListener(WMBusMessageListener wmBusMessageListener) {
        if (wmBusMessageListener == null) {
            return false;
        }

        logger.trace("register listener: Adding");
        boolean result = wmBusMessageListeners.add(wmBusMessageListener);
        logger.trace("register listener: Success");
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
        WMBusDevice decrypt = decrypt(device);

        // below we append thing handlers which are configured for given device address
        ArrayList<WMBusMessageListener> listeners = new ArrayList<>(this.wmBusMessageListeners);
        handlers.stream().filter(h -> device.getDeviceAddress().equals(h.getDeviceAddress()))
                .collect(Collectors.toCollection(() -> listeners));

        for (WMBusMessageListener wmBusMessageListener : listeners) {
            try {
                switch (type) {
                    case DEVICE_STATE_ADDED: {
                        wmBusMessageListener.onNewWMBusDevice(this, decrypt);
                        break;
                    }
                    case DEVICE_STATE_CHANGED: {
                        wmBusMessageListener.onChangedWMBusDevice(this, decrypt);
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
                    message.getVariableDataResponse().decode();
                    logger.info("Message from {} successfully decrypted, forwarding it to receivers",
                            device.getDeviceAddress());
                    return new WMBusDevice(message, this);
                } catch (DecodingException decodingException) {
                    logger.info(
                            "Could not decode frame, probably we still miss encryption key, forwarding frame in original form. {}, {}, {}",
                            decodingException.getMessage(), device.getOriginalMessage().toString(),
                            keyStorage.toMap().toString());
                } catch (NoClassDefFoundError decodingException) {
                    logger.info(
                            "Could not decode frame, probably we still miss encryption key, forwarding frame in original form. {}",
                            decodingException.getMessage());
                }
            } else if (parseException.getMessage().startsWith("Manufacturer specific CI:")
                    || parseException.getMessage().startsWith("Unable to decode message with this CI Field")) {
                logger.debug("Found frame with manufacturer specific encoding, forwarding for futher processing.");
            } else {
                logger.debug("Unexpected error while parsing frame, forwarding frame in original form", parseException);
            }
        }
        return device;
    }

    @Override
    public void processMessage(WMBusDevice device) {
        if (updateFrames.get()) {
            StringType frame = StringType.valueOf(HexUtils.bytesToHex(device.getOriginalMessage().asBlob()));
            getCallback().stateUpdated(new ChannelUID(getUID(), WMBusBindingConstants.CHANNEL_LAST_FRAME), frame);
        }
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

    @Override
    public ThingUID getUID() {
        return getThing().getUID();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (CHANNEL_LAST_FRAME.equals(channelUID.getId())) {
            updateFrames.set(true);
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        if (CHANNEL_LAST_FRAME.equals(channelUID.getId())) {
            updateFrames.set(false);
        }
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

    public void reset() {
        wmbusReceiver = null;
        initialize();
    }

    @Override
    public DateFieldMode getDateFieldMode() {
        return Optional.ofNullable(getConfigAs(WMBusBridgeConfig.class)).map(cfg -> cfg.dateFieldMode)
                .orElse(DateFieldMode.DATE_TIME);
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
