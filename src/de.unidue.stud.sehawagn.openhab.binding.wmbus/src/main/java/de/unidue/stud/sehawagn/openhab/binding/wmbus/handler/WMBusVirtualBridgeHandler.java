package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.VirtualWMBusMessageHelper;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusDevice;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusReceiver;

/**
 * This class represents the WMBus bridge and handles general events for the whole group of WMBus devices.
 */
public class WMBusVirtualBridgeHandler extends WMBusBridgeHandler {

    public static final String CHANNEL_CODE_VIRTUAL_BRIDGE = "wmbusvirtualbridge_code";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(WMBusBindingConstants.THING_TYPE_VIRTUAL_BRIDGE);

    private Logger logger = LoggerFactory.getLogger(WMBusVirtualBridgeHandler.class);

    public WMBusVirtualBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: "
                + command.toString());

        if (command == RefreshType.REFRESH) {
            logger.trace("handleCommand(): (2/5) command.refreshtype == REFRESH");
            State newState = UnDefType.NULL;
            String bytes = (String) getConfig().get(WMBusBindingConstants.CONFKEY_VIRTUAL_BYTES);
            WMBusMessage wmBusMessage = null;
            try {
                wmBusMessage = VirtualWMBusMessageHelper.decode(bytes.getBytes(), 100, new HashMap<>());
            } catch (DecodingException e) {
                logger.error(e.getMessage());
            }
            WMBusDevice wmBusDevice = null;
            if (wmBusMessage != null) {
                wmBusDevice = new WMBusDevice(wmBusMessage);
            }

            if (wmBusDevice != null) {
                logger.trace("handleCommand(): (3/5) deviceMessage != null");
                if (CHANNEL_CODE_VIRTUAL_BRIDGE.equals(channelUID.getId())) {
                    logger.trace("handleCommand(): (4/5): got a valid channel: VOLUME_INST_VAL");
                    // DataRecord record = wmBusDevice.findRecord(TYPE_CURRENT_VOLUME_INST_VAL);
                    // if (record != null) {
                    // newState = new DecimalType(record.getScaledDataValue());
                    // } else {
                    // logger.trace("handleCommand(): record not found in message");
                    // }
                } else {
                    logger.debug(
                            "handleCommand(): (4/5): no channel to put this value into found: " + channelUID.getId());
                }
                logger.trace("handleCommand(): (5/5) assigning new state to channel '" + channelUID.getId().toString()
                        + "': " + newState.toString());
                updateState(channelUID.getId(), newState);

            }

        }
    }

    /**
     * Connects to the WMBus radio module and updates bridge status.
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
     */
    @Override
    public void initialize() {
        logger.debug("WMBusVirtualBridgeHandler: initialize()");
        if (wmbusReceiver == null) {
            wmbusReceiver = new WMBusReceiver(this);
        }
        // success
        logger.debug("WMBusVirtualBridgeHandler: Initialization done! Setting bridge online");
        updateStatus(ThingStatus.ONLINE);

    }

}
