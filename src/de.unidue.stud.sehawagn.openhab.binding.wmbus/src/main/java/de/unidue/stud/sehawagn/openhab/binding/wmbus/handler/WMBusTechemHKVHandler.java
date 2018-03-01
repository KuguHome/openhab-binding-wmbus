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
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.TechemHKV;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusDevice;

public class WMBusTechemHKVHandler extends BaseThingHandler implements WMBusMessageListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_TECHEM_HKV);
    private final Logger logger = LoggerFactory.getLogger(WMBusTechemHKVHandler.class);
    private String deviceId;
    private WMBusBridgeHandler bridgeHandler;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private TechemHKV techemDevice;

    public WMBusTechemHKVHandler(Thing thing) {
        super(thing);
        logger.debug("WMBusThingHandler: new() for Thing" + thing.toString());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WMBusTechemHKVHandler handler.");
        Configuration config = getConfig();
        deviceId = (String) config.getProperties().get(PROPERTY_HKV_ID);
        WMBusDevice device = getDevice();
        if (device instanceof TechemHKV) {
            techemDevice = (TechemHKV) device;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing TechemHKV handler.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Thing handler: (1/4) command for channel " + channelUID.toString() + " command: " + command.toString());
        if (command == RefreshType.REFRESH) {
            logger.trace("Thing handler: handle command(): (2/4) command.refreshtype == REFRESH");
            State newState = UnDefType.NULL;

            if (techemDevice != null) {
                logger.trace("Thing handler: handle Command(): (3/4) deviceMessage != null");
                switch (channelUID.getId()) {
                    // TODO wie passiert die Kanalzuordnung - woher kommen diese Kanal-Nummern?
                    case CHANNEL_ROOMTEMPERATURE: {
                        logger.trace("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDevice.getT1());
                        break;
                    }
                    case CHANNEL_RADIATORTEMPERATURE: {
                        logger.trace("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDevice.getT2());
                        break;
                    }
                    case CHANNEL_CURRENTREADING: {
                        logger.trace("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDevice.getCurVal());
                        break;
                    }
                    case CHANNEL_LASTREADING: {
                        logger.trace("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDevice.getLastVal());
                        break;
                    }
                    case CHANNEL_RECEPTION: {
                        logger.trace("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDevice.getRssi());
                        break;
                    }
                    case CHANNEL_LASTDATE: {
                        logger.trace("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DateTimeType(techemDevice.getLastDate());
                        newState = new StringType(dateFormat.format(techemDevice.getLastDate().getTime()));
                        break;
                    }
                    case CHANNEL_CURRENTDATE: {
                        logger.trace("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new DateTimeType(techemDevice.getCurDate());
                        newState = new StringType(dateFormat.format(techemDevice.getCurDate().getTime()));
                        break;
                    }
                    case CHANNEL_ALMANAC: {
                        logger.trace("Thing handler: handleCommand(): (4/4): got a valid channel");
                        newState = new StringType(techemDevice.getHistory());
                        break;
                    }
                    default:
                        logger.debug("Thing handler: handleCommand(): (4/4): no channel to put this value into found");
                        break;
                }
                logger.trace("Thing handler: handleCommand(): assigning new state to channel");
                updateState(channelUID.getId(), newState);
            }
        }
    }

    private synchronized WMBusBridgeHandler getBridgeHandler() {
        logger.trace("thinghandler: getBridgeHandler() begin");
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
        logger.trace("thinghandler: getBridgeHandler() returning bridgehandler");
        return this.bridgeHandler;
    }

    private WMBusDevice getDevice() {
        logger.trace("thinghandler: getDevice() begin");
        WMBusBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("thinghandler: getDevice() end: returning null");
            return null;
        }
        logger.trace("thinghandler: getDevice() end: returning devicebyid");
        return bridgeHandler.getDeviceById(deviceId);
    }

    @Override
    public void onNewWMBusDevice(WMBusDevice wmBusDevice) {
        logger.trace("thinghandler: onNEwWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceId().equals(deviceId)) {
            logger.trace("thinghandler: onNEwWMBusDevice(): yes it's me");
            updateStatus(ThingStatus.ONLINE);
            onChangedWMBusDevice(wmBusDevice);
        }
        logger.trace("thinghandler: onNEwWMBusDevice(): no");
    }

    @Override
    public void onChangedWMBusDevice(WMBusDevice wmBusDevice) {
        logger.trace("thinghandler: onChangedWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceId().equals(deviceId)) {
            techemDevice = (TechemHKV) wmBusDevice;
            logger.trace("thinghandler: onChangedWMBusDevice(): inform all channels to refresh");
            for (Channel curChan : getThing().getChannels()) {
                handleCommand(curChan.getUID(), RefreshType.REFRESH);
            }
        }
        logger.trace("thinghandler: onChangedWMBusDevice(): return");
    }

}
