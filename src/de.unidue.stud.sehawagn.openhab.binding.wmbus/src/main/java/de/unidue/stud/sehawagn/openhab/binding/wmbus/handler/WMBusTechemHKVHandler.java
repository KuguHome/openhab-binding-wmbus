package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.TechemHKVMessage;

public class WMBusTechemHKVHandler extends BaseThingHandler implements WMBusMessageListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_TECHEM_HKV);
    private final Logger logger = LoggerFactory.getLogger(WMBusTechemHKVHandler.class);
    private String deviceId;
    private WMBusBridgeHandler bridgeHandler;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private TechemHKVMessage techemDeviceMessage;

    public WMBusTechemHKVHandler(Thing thing) {
        super(thing);
        logger.debug("WMBusThingHandler: new() for Thing" + thing.toString());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WMBusTechemHKVHandler handler.");
        Configuration config = getConfig();
        deviceId = (String) config.getProperties().get(PROPERTY_HKV_ID);
        WMBusMessage deviceMessage = getDevice();
        // TODO
        /*
         * if (deviceMessage instanceof TechemHKVMessage) {
         * techemDeviceMessage = (TechemHKVMessage) deviceMessage;
         * }
         */
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing TechemHKV handler.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Thing handler: (1/4) command for channel " + channelUID.toString() + " command: " + command.toString());
        if (command == RefreshType.REFRESH) {
            logger.debug("Thing handler: handle command(): (2/4) command.refreshtype == REFRESH");
            State newState = null;
            if (techemDeviceMessage != null) {
                logger.debug("Thing handler: handle Command(): (3/4) deviceMessage != null");
                switch (channelUID.getId()) {
                    //TODO wie passiert die Kanalzuordnung - woher kommen diese Kanal-Nummern?
                    case CHANNEL_ROOMTEMPERATURE: {
                        logger.debug("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDeviceMessage.getT1());
                        break;
                    }
                    case CHANNEL_RADIATORTEMPERATURE: {
                        logger.debug("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDeviceMessage.getT2());
                        break;
                    }
                    case CHANNEL_CURRENTREADING: {
                        logger.debug("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDeviceMessage.getCurVal());
                        break;
                    }
                    case CHANNEL_LASTREADING: {
                        logger.debug("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDeviceMessage.getLastVal());
                        break;
                    }
                    case CHANNEL_RECEPTION: {
                        logger.debug("Thing handler: handleCommand(): (4/4): got a valid channel");
                        // TODO
                        // newState = new DecimalType(techemDeviceMessage.getRssi());
                        break;
                    }
                    case CHANNEL_LASTDATE: {
                        logger.debug("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DateTimeType(techemDeviceMessage.getLastDate());
                        // newState = new StringType(dateFormat.format(techemDeviceMessage.getLastDate().getTime()));
                        break;
                    }
                    case CHANNEL_CURRENTDATE: {
                        logger.debug("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DateTimeType(techemDeviceMessage.getCurDate());
                        // newState = new StringType(dateFormat.format(techemDeviceMessage.getCurDate().getTime()));
                        break;
                    }
                    case CHANNEL_ALMANAC: {
                        logger.debug("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new StringType(techemDeviceMessage.getHistory());
                        break;
                    }
                    default:
                        logger.debug("Thing handler: handleCommand(): (4/4): no channel to put this value into found");
                        break;
                }
                logger.debug("Thing handler: handleCommand(): assigning new state to channel");
                updateState(channelUID.getId(), newState);
            }
        }
    }

    private synchronized WMBusBridgeHandler getBridgeHandler() {
        logger.debug("thinghandler: getBridgeHandler() begin");
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof WMBusBridgeHandler) {
                this.bridgeHandler = (WMBusBridgeHandler) handler;
                this.bridgeHandler.registerWMBusMessageListener(this);
            } else {
                return null;
            }
        }
        logger.debug("thinghandler: getBridgeHandler() returning bridgehandler");
        return this.bridgeHandler;
    }

    private WMBusMessage getDevice() {
        logger.debug("thinghandler: getDevice() begin");
        WMBusBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("thinghandler: getDevice() end: returning null");
            return null;
        }
        logger.debug("thinghandler: getDevice() end: returning devicebyid");
        return bridgeHandler.getDeviceById(deviceId);
    }

    @Override
    public void onNewWMBusDevice(WMBusMessage wmBusDevice) {
        logger.debug("thinghandler: onNEwWMBusDevice(): is it me?");
        if (wmBusDevice.getSecondaryAddress().getDeviceId().toString().equals(deviceId)) {
            logger.debug("thinghandler: onNEwWMBusDevice(): yes it's me");
            updateStatus(ThingStatus.ONLINE);
            onChangedWMBusDevice(wmBusDevice);
        }
        logger.debug("thinghandler: onNEwWMBusDevice(): no");
    }

    @Override
    public void onChangedWMBusDevice(WMBusMessage wmBusDevice) {
        logger.debug("thinghandler: onChangedWMBusDevice(): is it me?");
        if (wmBusDevice.getSecondaryAddress().getDeviceId().toString().equals(deviceId)) {
            // TODO
            // techemDeviceMessage = (TechemHKVMessage) wmBusDevice;
            logger.debug("thinghandler: onChangedWMBusDevice(): inform all channels to refresh");
            for (Channel curChan : getThing().getChannels()) {
                handleCommand(curChan.getUID(), RefreshType.REFRESH);
            }
        }
        logger.debug("thinghandler: onChangedWMBusDevice(): return");
    }
}
