package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import java.util.Collection;

import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.TechemReceiver;

public class WMBusBridgeHandler extends ConfigStatusBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(WMBusBridgeHandler.class);

    private TechemReceiver wmbusReceiver = null;

    // private ScheduledFuture<?> pollingJob;

    // private Runnable pollingRunnable = null;

    public WMBusBridgeHandler(Bridge bridge) {
        super(bridge);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // judging from the hue bridge, this seems to be not needed...?
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WMBus bridge handler.");
        // if (getConfig().get(WMBusBindingConstants.CONFKEY_INTERFACE_NAME) != null) {
        if (wmbusReceiver == null) {
            wmbusReceiver = new TechemReceiver();

            wmbusReceiver.setFilterIDs(new int[] { 92313948, 92363681, 92363684, 92363682, 92363688, 92363734 });

            // System.out.println("FOOBAR=");

            // for (String configKey : getConfig().getProperties().keySet()) {
            // System.out.println("config key=" + configKey + "value=" + getConfig().getProperties().get(configKey));
            //
            // }

            String interfaceName = (String) getConfig().get(WMBusBindingConstants.CONFKEY_INTERFACE_NAME);

            // logger.debug("Interface name=" + interfaceName);

            System.out.println("Interface name=" + interfaceName);

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
        onUpdate();
        // } else {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
        // "Cannot open WMBus device. Serial device name not given.");
        // }
    }

    @Override
    public void dispose() {
        logger.debug("WMBus bridge Handler disposed.");
        /*
         * if (pollingJob != null && !pollingJob.isCancelled()) {
         * pollingJob.cancel(true);
         * pollingJob = null;
         * }
         */
        if (wmbusReceiver != null) {
            wmbusReceiver = null;
        }
    }

    private synchronized void onUpdate() {
        if (wmbusReceiver != null) {
            /*
             * if (pollingJob == null || pollingJob.isCancelled()) {
             * int pollingInterval = WMBusBindingConstants.DEFAULT_POLLING_INTERVAL;
             * try {
             * Object pollingIntervalConfig = getConfig().get(WMBusBindingConstants.CONFKEY_POLLING_INTERVAL);
             * if (pollingIntervalConfig != null) {
             * pollingInterval = ((BigDecimal) pollingIntervalConfig).intValue();
             * } else {
             * logger.info("Polling interval not configured for this wmbus bridge. Using default value: {}s",
             * pollingInterval);
             * }
             * } catch (NumberFormatException ex) {
             * logger.info("Wrong configuration value for polling interval. Using default value: {}s",
             * pollingInterval);
             * }
             * pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 1, pollingInterval, TimeUnit.SECONDS);
             *
             * }
             */
        }
    }
}