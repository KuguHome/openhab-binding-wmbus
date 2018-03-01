package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DataRecord.DataValueType;
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

    public WMBusQundisQCaloricHandler(Thing thing) {
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
            techemDevice = device;
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
        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: " + command.toString());
        if (command == RefreshType.REFRESH) {
            logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (2/5) command.refreshtype == REFRESH");
            State newState = UnDefType.NULL;

            //TODO getOriginalMessage()
            //TODO ev. eigene Klasse als Pendant für TechemHKV
            //TODO generisch machen über Handler oder über WMBusDevice-Ableitungsklasse?

            if (techemDevice != null) {
                logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (3/5) deviceMessage != null");
                /*
                 * DIB:0B, VIB:6E -> descr:HCA, function:INST_VAL, value:000000, unit:RESERVED -- current reading
                 * DIB:4B, VIB:6E -> descr:HCA, function:INST_VAL, storage:1, value:000000, unit:RESERVED -- previous reading?
                 * DIB:42, VIB:6C -> descr:DATE, function:INST_VAL, storage:1, value:Wed Mar 31 00:00:00 CEST 2128 -- previous reading?
                 * DIB:CB08, VIB:6E -> descr:HCA, function:INST_VAL, storage:17, value:000000, unit:RESERVED -- previous reading?
                 * DIB:C208, VIB:6C -> descr:DATE, function:INST_VAL, storage:17, value:Thu Nov 30 00:00:00 CET 2017 -- previous reading?
                 * DIB:32, VIB:6C -> descr:DATE, function:ERROR_VAL, value:Sat Sep 23 00:00:00 CEST 2017 -- time of last error
                 * DIB:04, VIB:6D -> descr:DATE_TIME, function:INST_VAL, value:Wed Dec 13 17:25:00 CET 2017 -- current time
                 */
                switch (channelUID.getId()) {
                    case CHANNEL_ROOMTEMPERATURE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: ROOMTEMPERATURE");
                        //TODO newState = new DecimalType(techemDevice.getT1());
                        break;
                    }
                    case CHANNEL_RADIATORTEMPERATURE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: RADIATORTEMPERATURE");
                        //TODO newState = new DecimalType(techemDevice.getT2());
                        break;
                    }
                    case CHANNEL_CURRENTREADING: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: CURRENTREADING");
                        // DIB:0B, VIB:6E
                        DataRecord record = findRecord(new byte[] { 0x0b }, new byte[] { 0x6e });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        }
                        break;
                    }
                    case CHANNEL_LASTREADING: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: LASTREADING");
                        //TODO
                        //newState = new DecimalType(techemDevice.getLastVal());
                        break;
                    }
                    case CHANNEL_RECEPTION: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: RECEPTION");
                        newState = new DecimalType(techemDevice.getOriginalMessage().getRssi());
                        break;
                    }
                    case CHANNEL_LASTDATE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: LASTDATE");
                        //newState = new DateTimeType(techemDevice.getLastDate());
                        //newState = new StringType(dateFormat.format(techemDevice.getLastDate().getTime()));
                        break;
                    }
                    case CHANNEL_CURRENTDATE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: CURRENTDATE");
                        //newState = new DateTimeType(techemDevice.getCurDate());
                        //newState = new StringType(dateFormat.format(techemDevice.getCurDate().getTime()));
                        // DIB:04, VIB:6D
                        DataRecord record = findRecord(new byte[] { 0x04 }, new byte[] { 0x6d });
                        if (record != null && record.getDataValueType() == DataValueType.DATE) {
                            //TODO String oder DateTimeType besser?
                            Date date = (java.util.Date) record.getDataValue();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            newState = new DateTimeType(cal);
                        }
                        break;
                    }
                    case CHANNEL_ALMANAC: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: ALMANAC");
                        newState = new StringType("TODO-ALMANAC"); //TODO new StringType(techemDevice.getHistory());
                        break;
                    }
                    default:
                        logger.debug("WMBusQundisQCaloricHandler: handleCommand(): (4/5): no channel to put this value into found: " + channelUID.getId());
                        break;
                }
                logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (5/5) assigning new state to channel '" + channelUID.getId().toString() + "': " + newState.toString());
                updateState(channelUID.getId(), newState);
            }
        }
    }

    private DataRecord findRecord(byte[] dib, byte[] vib) {
        for (DataRecord record : this.techemDevice.getOriginalMessage().getVariableDataResponse().getDataRecords()) {
            if (Arrays.equals(record.getDib(), dib) && Arrays.equals(record.getVib(), vib)) {
                return record;
            }
        }
        return null;
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
        logger.trace("WMBusQundisQCaloricHandler: onNewWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceId().equals(deviceId)) {
            logger.trace("WMBusQundisQCaloricHandler: onNewWMBusDevice(): yes it's me");
            logger.trace("WMBusQundisQCaloricHandler: onNewWMBusDevice(): updating status to online");
            updateStatus(ThingStatus.ONLINE);
            logger.trace("WMBusQundisQCaloricHandler: onNewWMBusDevice(): calling onChangedWMBusDevice()");
            onChangedWMBusDevice(wmBusDevice);
        }
        logger.trace("WMBusQundisQCaloricHandler: onNewWMBusDevice(): no");
    }

    @Override
    public void onChangedWMBusDevice(WMBusDevice wmBusDevice) {
        logger.trace("WMBusQundisQCaloricHandler: onChangedWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceId().equals(deviceId)) {
            //techemDevice = (TechemHKV) wmBusDevice;
            // TODO
            techemDevice = wmBusDevice;
            logger.trace("WMBusQundisQCaloricHandler: onChangedWMBusDevice(): inform all channels to refresh");
            // refresh all channels -> handleCommand()
            for (Channel curChan : getThing().getChannels()) {
                handleCommand(curChan.getUID(), RefreshType.REFRESH);
            }
        }
        logger.trace("WMBusQundisQCaloricHandler: onChangedWMBusDevice(): return");
    }
}
