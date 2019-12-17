/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.handler;

import static org.openhab.binding.wmbus.WMBusBindingConstants.CHANNEL_LAST_FRAME;

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
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.internal.WMBusReceiver;
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
                    WMBusMessage message = VirtualWMBusMessageHelper
                            .decode(device.getOriginalMessage().asBlob(), device.getOriginalMessage().getRssi(),
                                    keyStorage.toMap());
                    message.getVariableDataResponse().decode();
                    logger.info("Message from {} successfully decrypted, forwarding it to receivers",
                            device.getDeviceAddress());
                    return new WMBusDevice(message, this);
                } catch (DecodingException decodingException) {
                    logger.info(
                            "Could not decode frame, probably we still miss encryption key, forwarding frame in original form. {}",
                            decodingException.getMessage());
                }
            } else if (parseException.getMessage().startsWith("Manufacturer specific CI:") || parseException
                    .getMessage().startsWith("Unable to decode message with this CI Field")) {
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

    protected int[] parseDeviceIDFilter() {
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

    static class StatusRunnable implements Runnable {

        private final Set<WMBusDeviceHandler<WMBusDevice>> handlers;

        public StatusRunnable(Set<WMBusDeviceHandler<WMBusDevice>> handlers) {
            this.handlers = handlers;
        }

        @Override public void run() {
            handlers.stream().forEach(WMBusDeviceHandler::checkStatus);
        }

    }

}
