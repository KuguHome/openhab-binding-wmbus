package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.TechemReceiver;

public class WMBusBridgeHandler extends ConfigStatusBridgeHandler {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_WMBUS_BRIDGE);

    private Logger logger = LoggerFactory.getLogger(WMBusBridgeHandler.class);

    private TechemReceiver wmbusReceiver = null;

    private List<WMBusMessageListener> wmBusMessageListeners = new CopyOnWriteArrayList<>();

    public WMBusBridgeHandler(Bridge bridge) {
        super(bridge);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        return Collections.emptyList(); // all good, otherwise add some messages
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // judging from the hue bridge, this seems to be not needed...?
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WMBus bridge handler.");
        if (getConfig().get(CONFKEY_INTERFACE_NAME) != null) {
            if (wmbusReceiver == null) {
                wmbusReceiver = new TechemReceiver(this);

                // wmbusReceiver.setFilterIDs(new int[] { 92313948, 92363681, 92363684, 92363682, 92363688, 92363734 });

                // System.out.println("FOOBAR=");

                // for (String configKey : getConfig().getProperties().keySet()) {
                // System.out.println("config key=" + configKey + "value=" +
                // getConfig().getProperties().get(configKey));
                //
                // }

                String interfaceName = (String) getConfig().get(CONFKEY_INTERFACE_NAME);

                // logger.debug("Interface name=" + interfaceName);

                // System.out.println("Interface name=" + interfaceName);

                wmbusReceiver.init(interfaceName);

                updateStatus(ThingStatus.ONLINE);

                // wmbusReceiver.setTimeout(5000);
                /*
                 * pollingRunnable = new Runnable() {
                 *
                 * @Override
                 * public void run() {
                 * try {
                 * // TODO get the WMBus messages from the receiver?
                 * } catch (Throwable t) {
                 * logger.error("An unexpected error occurred: {}", t.getMessage(), t);
                 * }
                 * }
                 * };
                 */
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot open WMBus device. Serial device name not given.");
        }
    }

    public boolean registerWMBusMessageListener(WMBusMessageListener wmBusMessageListener) {
        if (wmBusMessageListener == null) {
            throw new NullPointerException("It's not allowed to pass a null WMBusMessageListener.");
        }
        boolean result = wmBusMessageListeners.add(wmBusMessageListener);

        return result;
    }

    /**
     * Iterate through wmBusMessageListeners and notify them about a newly received message.
     *
     * @param message
     */
    private void notifyWMBusMessageListeners(final WMBusMessage message) {
        for (WMBusMessageListener wmBusMessageListener : wmBusMessageListeners) {
            try {
                wmBusMessageListener.onWMBusMessageReceived(message);
                break;
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
        notifyWMBusMessageListeners(message);
    }

}