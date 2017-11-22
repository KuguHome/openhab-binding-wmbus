package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

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
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openmuc.jmbus.WMBusMessage;
import org.openmuc.jmbus.WMBusMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.TechemReceiver;

/**
 * This class represents the WMBus bridge and handles general events for the whole group of WMBus devices.
 */
public class WMBusBridgeHandler extends ConfigStatusBridgeHandler {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private static final String DEVICE_STATE_ADDED = "added";

    private static final String DEVICE_STATE_CHANGED = "changed";

    private Logger logger = LoggerFactory.getLogger(WMBusBridgeHandler.class);

    private TechemReceiver wmbusReceiver = null;

    private Map<String, WMBusMessage> knownDevices = new HashMap<>();

    private List<WMBusMessageListener> wmBusMessageListeners = new CopyOnWriteArrayList<>();

    public WMBusBridgeHandler(Bridge bridge) {
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
        logger.debug("Initializing WMBus bridge handler.");

        // check serial device name
        if (!getConfig().containsKey(CONFKEY_INTERFACE_NAME)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot open WMBus device. Serial device name not given.");
            return;
        }

        // check radio mode
        if (!getConfig().containsKey(CONFKEY_RADIO_MODE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot open WMBus device. Radio mode not given.");
            return;
        }

        // set up WMBus receiver = handler for radio telegrams
        if (wmbusReceiver == null) {
            wmbusReceiver = new TechemReceiver(this);

            String interfaceName = (String) getConfig().get(CONFKEY_INTERFACE_NAME);
            String radioModeStr = (String) getConfig().get(CONFKEY_RADIO_MODE);
            WMBusMode radioMode;

            // check and convert radio mode
            switch (radioModeStr) {
                case "S":
                    radioMode = WMBusMode.S;
                    break;
                case "T":
                    radioMode = WMBusMode.T;
                    break;
                default:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "Cannot open WMBus device. Unknown radio mode given: " + radioModeStr
                                    + ". Expected 'S' or 'T'.");
                    return;
            }

            // connect to the radio module
            try {
                wmbusReceiver.init(interfaceName, radioMode);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Cannot open WMBus device: " + e.getMessage());
                return;
            }

            // success
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public boolean registerWMBusMessageListener(WMBusMessageListener wmBusMessageListener) {
        if (wmBusMessageListener == null) {
            throw new NullPointerException("It's not allowed to pass a null WMBusMessageListener.");
        }
        boolean result = wmBusMessageListeners.add(wmBusMessageListener);
        if (result) {
            // inform the listener initially about all devices and their states
            for (WMBusMessage device : knownDevices.values()) {
                wmBusMessageListener.onNewWMBusDevice(device);
            }
        }
        return result;
    }

    /**
     * Iterate through wmBusMessageListeners and notify them about a newly received message.
     *
     * @param message
     */
    private void notifyWMBusMessageListeners(final WMBusMessage message, final String type) {
        for (WMBusMessageListener wmBusMessageListener : wmBusMessageListeners) {
            try {
                switch (type) {
                    case DEVICE_STATE_ADDED: {
                        wmBusMessageListener.onNewWMBusDevice(message);
                        break;
                    }
                    case DEVICE_STATE_CHANGED: {
                        wmBusMessageListener.onChangedWMBusDevice(message);
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
    }

    @Override
    public void dispose() {
        logger.debug("WMBus bridge Handler disposed.");

        if (wmbusReceiver != null) {
            wmbusReceiver = null;
        }
    }

    public void processMessage(WMBusMessage message) {
        String deviceId = message.getSecondaryAddress().getDeviceId().toString();
        String deviceState = DEVICE_STATE_ADDED;
        if (knownDevices.containsKey(deviceId)) {
            deviceState = DEVICE_STATE_CHANGED;
        }
        knownDevices.put(deviceId, message);
        notifyWMBusMessageListeners(message, deviceState);
    }

    public WMBusMessage getDeviceById(String deviceId) {
        return knownDevices.get(deviceId);
    }

}