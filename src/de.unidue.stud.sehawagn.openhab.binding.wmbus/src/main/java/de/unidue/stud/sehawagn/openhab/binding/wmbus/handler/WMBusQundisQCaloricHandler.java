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

// Thing resp. device handler for the Qundis Qcaloric 5,5 heat cost allocator (Heizkostenverteiler)

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

            //TODO generisch machen über abgeleitete Handler oder über 1 generischen Handler mit gerätespezifischen WMBusDevice-Ableitungsklassen?

            if (techemDevice != null) {
                logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (3/5) deviceMessage != null");
                //TODO reduce channels to only what this device can provide
                /*
                 * DIB:0B, VIB:6E -> descr:HCA, function:INST_VAL, value:000000, unit:RESERVED -- current reading / measurement value
                 * DIB:4B, VIB:6E -> descr:HCA, function:INST_VAL, storage:1, value:000000, unit:RESERVED -- reading before previous one
                 * DIB:42, VIB:6C -> descr:DATE, function:INST_VAL, storage:1, value:Sun Dec 31 00:00:00 CET 2017 -- reading before previous one
                 * DIB:CB08, VIB:6E -> descr:HCA, function:INST_VAL, storage:17, value:000000, unit:RESERVED  -- previous reading
                 * DIB:C208, VIB:6C -> descr:DATE, function:INST_VAL, storage:17, value:Wed Feb 28 00:00:00 CET 2018 -- previous reading
                 * DIB:32, VIB:6C -> descr:DATE, function:ERROR_VAL, value:Sat Sep 23 00:00:00 CEST 2017 -- time of last error
                 * DIB:04, VIB:6D -> descr:DATE_TIME, function:INST_VAL, value:Thu Mar 01 21:12:00 CET 2018 -- timestamp of current / latest reading
                 */
                switch (channelUID.getId()) {
                    case CHANNEL_RECEPTION: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: RECEPTION");
                        newState = new DecimalType(techemDevice.getOriginalMessage().getRssi());
                        break;
                    }
                    //TODO put all the date value conversions into an helper method
                    //TODO do not instantiate new byte arrays each time (re-use / convert to constants)
                    case CHANNEL_ERRORDATE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: ERRORDATE");
                        DataRecord record = findRecord(new byte[] { 0x32 }, new byte[] { 0x6c });
                        if (record != null && record.getDataValueType() == DataValueType.DATE) {
                            Date date = (java.util.Date) record.getDataValue();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            newState = new DateTimeType(cal);
                        } else {
                            logger.trace("WMBusQundisQCaloricHandler: handleCommand(): record not found in message or not of type date");
                        }
                        break;
                    }
                    case CHANNEL_CURRENTREADING: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: CURRENTREADING");
                        DataRecord record = findRecord(new byte[] { 0x0b }, new byte[] { 0x6e });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("WMBusQundisQCaloricHandler: handleCommand(): record not found in message");
                        }
                        break;
                    }
                    case CHANNEL_CURRENTDATE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: CURRENTDATE");
                        DataRecord record = findRecord(new byte[] { 0x04 }, new byte[] { 0x6d });
                        if (record != null && record.getDataValueType() == DataValueType.DATE) {
                            Date date = (java.util.Date) record.getDataValue();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            newState = new DateTimeType(cal);
                        } else {
                            logger.trace("WMBusQundisQCaloricHandler: handleCommand(): record not found in message or not of type date");
                        }
                        break;
                    }
                    //TODO improve naming: last reading <--> previous reading (need to find out what is actually reported by this device as previous reading)
                    case CHANNEL_PREVIOUSREADING: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: PREVIOUSREADING");
                        //TODO why is type cast at 0xCB required? 0xCB already is a byte, right?
                        DataRecord record = findRecord(new byte[] { (byte) 0xCB, 0x08 }, new byte[] { 0x6e });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("WMBusQundisQCaloricHandler: handleCommand(): record not found in message");
                        }
                        break;
                    }
                    case CHANNEL_PREVIOUSDATE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: PREVIOUSDATE");
                        //TODO why is type cast at 0xC2 required? 0xCB already is a byte, right?
                        DataRecord record = findRecord(new byte[] { (byte) 0xC2, 0x08 }, new byte[] { 0x6c });
                        if (record != null && record.getDataValueType() == DataValueType.DATE) {
                            Date date = (java.util.Date) record.getDataValue();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            newState = new DateTimeType(cal);
                        } else {
                            logger.trace("WMBusQundisQCaloricHandler: handleCommand(): record not found in message or not of type date");
                        }
                        break;
                    }
                    case CHANNEL_LASTREADING: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: LASTREADING");
                        DataRecord record = findRecord(new byte[] { 0x4b }, new byte[] { 0x6e });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("WMBusQundisQCaloricHandler: handleCommand(): record not found in message");
                        }
                        break;
                    }
                    case CHANNEL_LASTDATE: {
                        logger.trace("WMBusQundisQCaloricHandler: handleCommand(): (4/5): got a valid channel: LASTDATE");
                        DataRecord record = findRecord(new byte[] { 0x42 }, new byte[] { 0x6c });
                        if (record != null && record.getDataValueType() == DataValueType.DATE) {
                            Date date = (java.util.Date) record.getDataValue();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            newState = new DateTimeType(cal);
                        } else {
                            logger.trace("WMBusQundisQCaloricHandler: handleCommand(): record not found in message or not of type date");
                        }
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
            techemDevice = wmBusDevice;
            logger.trace("WMBusQundisQCaloricHandler: onChangedWMBusDevice(): yes -> inform all channels to refresh");
            // refresh all channels -> handleCommand()
            for (Channel curChan : getThing().getChannels()) {
                handleCommand(curChan.getUID(), RefreshType.REFRESH);
            }
        } else {
            logger.trace("WMBusQundisQCaloricHandler: onChangedWMBusDevice(): no");
        }
        logger.trace("WMBusQundisQCaloricHandler: onChangedWMBusDevice(): return");
    }
}
