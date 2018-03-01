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

public class WMBusQundisQCaloricHandler extends BaseThingHandler implements WMBusMessageListener {

    // must set this for add new device handlers
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_QUNDIS_QCALORIC_5_5);
    private final Logger logger = LoggerFactory.getLogger(WMBusQundisQCaloricHandler.class);
    private String deviceId;
    private WMBusBridgeHandler bridgeHandler;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private WMBusDevice techemDevice;

    public WMBusTechemHKVHandler(Thing thing) {
        super(thing);
        logger.debug("WMBusQundisQCaloricHandler: new() for Thing" + thing.toString());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WMBusQundisQCaloricHandler handler.");
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
        logger.debug("Disposing WMBusQundisQCaloricHandler handler.");
    }

    // gets affected from onChangedWMBusDevice()
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("WMBusQundisQCaloricHandler: (1/4) command for channel " + channelUID.toString() + " command: " + command.toString());
        if (command == RefreshType.REFRESH) {
            logger.trace("WMBusQundisQCaloricHandler: handle command(): (2/4) command.refreshtype == REFRESH");
            State newState = UnDefType.NULL;
            
            //TODO getOriginalMessage()
            //TODO ev. eigene Klasse als Pendant für TechemHKV
            //TODO generisch machen über Handler oder über WMBusDevice-Ableitungsklasse?

            if (techemDevice != null) {
                logger.trace("WMBusQundisQCaloricHandler: handle Command(): (3/4) deviceMessage != null");
                switch (channelUID.getId()) {
                    case CHANNEL_ROOMTEMPERATURE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDevice.getT1());
                        break;
                    }
                    case CHANNEL_RADIATORTEMPERATURE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDevice.getT2());
                        break;
                    }
                    case CHANNEL_CURRENTREADING: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDevice.getCurVal());
                        break;
                    }
                    case CHANNEL_LASTREADING: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDevice.getLastVal());
                        break;
                    }
                    case CHANNEL_RECEPTION: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/4): got a valid channel");
                        newState = new DecimalType(techemDevice.getRssi());
                        break;
                    }
                    case CHANNEL_LASTDATE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/4): got a valid channel");
                        newState = new DateTimeType(techemDevice.getLastDate());
                        newState = new StringType(dateFormat.format(techemDevice.getLastDate().getTime()));
                        break;
                    }
                    case CHANNEL_CURRENTDATE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/4): got a valid channel");
                        newState = new DateTimeType(techemDevice.getCurDate());
                        newState = new StringType(dateFormat.format(techemDevice.getCurDate().getTime()));
                        break;
                    }
                    case CHANNEL_ALMANAC: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/4): got a valid channel");
                        newState = new StringType(techemDevice.getHistory());
                        break;
                    }
                    default:
                        logger.debug("WMBusQundisQCaloricHandler: handleCommand(): (4/4): no channel to put this value into found");
                        break;
                }
                logger.trace("WMBusQundisQCaloricHandler: handleCommand(): assigning new state to channel");
                updateState(channelUID.getId(), newState);
            }
        }
    }

    private synchronized WMBusBridgeHandler getBridgeHandler() {
        logger.trace("WMBusQundisQCaloricHandler: getBridgeHandler() begin");
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
        logger.trace("WMBusQundisQCaloricHandler: getBridgeHandler() returning bridgehandler");
        return this.bridgeHandler;
    }

    private WMBusDevice getDevice() {
        logger.trace("WMBusQundisQCaloricHandler: getDevice() begin");
        WMBusBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("thinghandler: getDevice() end: returning null");
            return null;
        }
        logger.trace("WMBusQundisQCaloricHandler: getDevice() end: returning devicebyid");
        return bridgeHandler.getDeviceById(deviceId);
    }

    // entry point - gets device here
    @Override
    public void onNewWMBusDevice(WMBusDevice wmBusDevice) {
        logger.trace("WMBusQundisQCaloricHandler: onNEwWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceId().equals(deviceId)) {
            logger.trace("WMBusQundisQCaloricHandler: onNEwWMBusDevice(): yes it's me");
            updateStatus(ThingStatus.ONLINE);
            onChangedWMBusDevice(wmBusDevice);
        }
        logger.trace("WMBusQundisQCaloricHandler: onNEwWMBusDevice(): no");
    }

    @Override
    public void onChangedWMBusDevice(WMBusDevice wmBusDevice) {
        logger.trace("WMBusQundisQCaloricHandler: onChangedWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceId().equals(deviceId)) {
            techemDevice = (TechemHKV) wmBusDevice;
            logger.trace("WMBusQundisQCaloricHandler: onChangedWMBusDevice(): inform all channels to refresh");
            // refresh all channels -> handleCommand()
            for (Channel curChan : getThing().getChannels()) {
                handleCommand(curChan.getUID(), RefreshType.REFRESH);
            }
        }
        logger.trace("WMBusQundisQCaloricHandler: onChangedWMBusDevice(): return");
    }

}
