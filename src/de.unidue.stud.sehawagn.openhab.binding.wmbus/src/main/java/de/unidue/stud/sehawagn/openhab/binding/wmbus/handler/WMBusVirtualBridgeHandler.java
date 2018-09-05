package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openmuc.jmbus.wireless.WMBusConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusDevice;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusReceiver;

/**
 * This class represents the WMBus bridge and handles general events for the whole group of WMBus devices.
 */
public class WMBusVirtualBridgeHandler extends WMBusBridgeHandler {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(WMBusBindingConstants.THING_TYPE_VIRTUAL_BRIDGE);

    private static final String DEVICE_STATE_ADDED = "added";

    private static final String DEVICE_STATE_CHANGED = "changed";

    private Logger logger = LoggerFactory.getLogger(WMBusVirtualBridgeHandler.class);

    private WMBusReceiver wmbusReceiver = null;
    private WMBusConnection wmbusConnection = null;

    private Map<String, WMBusDevice> knownDevices = new HashMap<>();

    private List<WMBusMessageListener> wmBusMessageListeners = new CopyOnWriteArrayList<>();

    public WMBusVirtualBridgeHandler(Bridge bridge) {
        super(bridge);
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
        logger.debug("WMBusVirtualBridgeHandler: initialize()");

        // success
        logger.debug("WMBusVirtualBridgeHandler: Initialization done! Setting bridge online");
        updateStatus(ThingStatus.ONLINE);

    }

    @Override
    public boolean registerWMBusMessageListener(WMBusMessageListener wmBusMessageListener) {
        if (wmBusMessageListener == null) {
            throw new NullPointerException("It's not allowed to pass a null WMBusMessageListener.");
        }
        logger.debug("register listener: Adding");
        boolean result = wmBusMessageListeners.add(wmBusMessageListener);
        logger.debug("register listener: Success");
        if (result) {
            // inform the listener initially about all devices and their states
            for (WMBusDevice device : knownDevices.values()) {
                wmBusMessageListener.onNewWMBusDevice(device);
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
                        wmBusMessageListener.onNewWMBusDevice(device);
                        break;
                    }
                    case DEVICE_STATE_CHANGED: {
                        wmBusMessageListener.onChangedWMBusDevice(device);
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
        logger.debug("WMBus virtual bridge Handler disposed.");

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
    }

    @Override
    public void processMessage(WMBusDevice device) {
        logger.trace("bridge: processMessage begin");
        String deviceId = device.getDeviceId();
        String deviceState = DEVICE_STATE_ADDED;
        if (knownDevices.containsKey(deviceId)) {
            deviceState = DEVICE_STATE_CHANGED;
        }
        knownDevices.put(deviceId, device);
        logger.trace("bridge processMessage: notifying listeners");
        notifyWMBusMessageListeners(device, deviceState);
        logger.trace("bridge: processMessage end");
    }

    @Override
    public WMBusDevice getDeviceById(String deviceId) {
        logger.trace("bridge: get device by id: " + deviceId);
        if (knownDevices.containsKey(deviceId)) {
            logger.trace("bridge: found device");
        } else {
            logger.trace("bridge: device not found");
        }
        return knownDevices.get(deviceId);
    }

}
