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

public class WMBusKamstrupMultiCal302Handler extends BaseThingHandler implements WMBusMessageListener {

    // must set this for add new device handlers
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_KAMSTRUP_MULTICAL_302);
    private final Logger logger = LoggerFactory.getLogger(WMBusKamstrupMultiCal302Handler.class);
    private String deviceId;
    private WMBusBridgeHandler bridgeHandler;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private WMBusDevice techemDevice;

    public WMBusKamstrupMultiCal302Handler(Thing thing) {
        super(thing);
        logger.debug("WMBusKamstrupMultiCal302Handler: new() for Thing" + thing.toString());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WMBusKamstrupMultiCal302Handler handler.");
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
        logger.debug("Disposing WMBusKamstrupMultiCal302Handler handler.");
    }

    // gets affected from onChangedWMBusDevice()
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: " + command.toString());
        if (command == RefreshType.REFRESH) {
            logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): (2/5) command.refreshtype == REFRESH");
            State newState = UnDefType.NULL;

            //TODO generisch machen über abgeleitete Handler oder über 1 generischen Handler mit gerätespezifischen WMBusDevice-Ableitungsklassen?

            if (techemDevice != null) {
                logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): (3/5) deviceMessage != null");
                /* TODO
                 * DIB:03, VIB:06 -> descr:ENERGY, function:INST_VAL, scaled value:0.0, unit:WATT_HOUR, Wh -- current energy total reading in Wh
                 * DIB:43, VIB:06 -> descr:ENERGY, function:INST_VAL, storage:1, scaled value:0.0, unit:WATT_HOUR, Wh -- previous energy total reading in Wh
                 * DIB:03, VIB:14 -> descr:VOLUME, function:INST_VAL, scaled value:0.0, unit:CUBIC_METRE, m³ -- current water volume total reading in m³
                 * DIB:42, VIB:6C -> descr:DATE, function:INST_VAL, storage:1, value:Thu Nov 30 00:00:00 CET 2017  -- previous reading timestamp
                 * DIB:02, VIB:2D -> descr:POWER, function:INST_VAL, scaled value:0.0, unit:WATT, W -- current water heat power reading in W
                 * DIB:01, VIB:FF21 -> descr:MANUFACTURER_SPECIFIC, function:INST_VAL, value:16 -- currently not interpreted
                 */
                switch (channelUID.getId()) {
                    case CHANNEL_RECEPTION: {
                        logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): (4/5): got a valid channel: RECEPTION");
                        newState = new DecimalType(techemDevice.getOriginalMessage().getRssi());
                        break;
                    }
                    //TODO put all the date value conversions into an helper method
                    //TODO do not instantiate new byte arrays each time (re-use / convert to constants)
                    case CHANNEL_CURRENTPOWER: {
                        logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): (4/5): got a valid channel: CURRENTPOWER");
                        DataRecord record = findRecord(new byte[] { 0x02 }, new byte[] { 0x2d });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): record not found in message");
                        }
                        break;
                    }
                    case CHANNEL_CURRENTENERGYTOTAL: {
                        logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): (4/5): got a valid channel: CURRENTENERGYTOTAL");
                        DataRecord record = findRecord(new byte[] { 0x03 }, new byte[] { 0x06 });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue() / 1000); // Wh to kWh (usual value unit)
                        } else {
                            logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): record not found in message");
                        }
                        break;
                    }
                    case CHANNEL_CURRENTVOLUMETOTAL: {
                        logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): (4/5): got a valid channel: CURRENTVOLUMETOTAL");
                        DataRecord record = findRecord(new byte[] { 0x03 }, new byte[] { 0x14 });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): record not found in message");
                        }
                        break;
                    }
                    // NOTE: device does not publish current date - using now
                    case CHANNEL_CURRENTDATE: {
                        logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): (4/5): got a valid channel: CURRENTDATE");
                        // if the main reading is set, then also return a current date, otherwise this would return the current date on every channel refresh
                        DataRecord record = findRecord(new byte[] { 0x43 }, new byte[] { 0x06 });
                        if (record != null) {
                            // now in current time zone and default locale
                            Calendar cal = Calendar.getInstance();
                            newState = new DateTimeType(cal);
                        } else {
                            logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): record not found in message or not of type date");
                        }
                        break;
                    }
                    case CHANNEL_PREVIOUSENERGYTOTAL: {
                        logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): (4/5): got a valid channel: PREVIOUSENERGYTOTAL");
                        DataRecord record = findRecord(new byte[] { 0x43 }, new byte[] { 0x06 });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue() / 1000); // Wh to kWh (usual value unit)
                        } else {
                            logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): record not found in message");
                        }
                        break;
                    }
                    case CHANNEL_PREVIOUSDATE: {
                        logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): (4/5): got a valid channel: PREVIOUSDATE");
                        DataRecord record = findRecord(new byte[] { 0x42 }, new byte[] { 0x6c });
                        if (record != null && record.getDataValueType() == DataValueType.DATE) {
                            Date date = (java.util.Date) record.getDataValue();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            newState = new DateTimeType(cal);
                        } else {
                            logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): record not found in message or not of type date");
                        }
                        break;
                    }
                    default:
                        logger.debug("WMBusKamstrupMultiCal302Handler: handleCommand(): (4/5): no channel to put this value into found: " + channelUID.getId());
                        break;
                }
                logger.trace("WMBusKamstrupMultiCal302Handler: handleCommand(): (5/5) assigning new state to channel '" + channelUID.getId().toString() + "': " + newState.toString());
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
        logger.trace("WMBusKamstrupMultiCal302Handler: getBridgeHandler() begin");
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
        logger.trace("WMBusKamstrupMultiCal302Handler: getBridgeHandler() returning bridgehandler");
        return this.bridgeHandler;
    }

    private WMBusDevice getDevice() {
        logger.trace("WMBusKamstrupMultiCal302Handler: getDevice() begin");
        WMBusBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("thinghandler: getDevice() end: returning null");
            return null;
        }
        logger.trace("WMBusKamstrupMultiCal302Handler: getDevice() end: returning devicebyid");
        return bridgeHandler.getDeviceById(deviceId);
    }

    // entry point - gets device here
    @Override
    public void onNewWMBusDevice(WMBusDevice wmBusDevice) {
        logger.trace("WMBusKamstrupMultiCal302Handler: onNewWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceId().equals(deviceId)) {
            logger.trace("WMBusKamstrupMultiCal302Handler: onNewWMBusDevice(): yes it's me");
            logger.trace("WMBusKamstrupMultiCal302Handler: onNewWMBusDevice(): updating status to online");
            updateStatus(ThingStatus.ONLINE);
            logger.trace("WMBusKamstrupMultiCal302Handler: onNewWMBusDevice(): calling onChangedWMBusDevice()");
            onChangedWMBusDevice(wmBusDevice);
        }
        logger.trace("WMBusKamstrupMultiCal302Handler: onNewWMBusDevice(): no");
    }

    @Override
    public void onChangedWMBusDevice(WMBusDevice wmBusDevice) {
        logger.trace("WMBusKamstrupMultiCal302Handler: onChangedWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceId().equals(deviceId)) {
            techemDevice = wmBusDevice;
            logger.trace("WMBusKamstrupMultiCal302Handler: onChangedWMBusDevice(): yes");
            // in between the good messages, there are messages with unvalid values -> filter these out
            if (!this.checkMessage(wmBusDevice)) {
                logger.trace("WMBusKamstrupMultiCal302Handler: onChangedWMBusDevice(): this is a malformed message, ignoring this message");
            } else {
                // refresh all channels -> handleCommand()
                logger.trace("WMBusKamstrupMultiCal302Handler: onChangedWMBusDevice(): inform all channels to refresh");
                for (Channel curChan : getThing().getChannels()) {
                    handleCommand(curChan.getUID(), RefreshType.REFRESH);
                }
            }
        } else {
            logger.trace("WMBusKamstrupMultiCal302Handler: onChangedWMBusDevice(): no");
        }
        logger.trace("WMBusKamstrupMultiCal302Handler: onChangedWMBusDevice(): return");
    }

    // in between the good messages, there are messages with unvalid values, filter these.
    //  previous date in the future
    //  any of the Wh values are negative
    //  number of data records is 0 -> leads to channels being set to NULL
    // -> filter these out
    private boolean checkMessage(WMBusDevice wmBusDevice) {
        DataRecord record;

        // check for number of records being too low to be valid
        if (wmBusDevice.getOriginalMessage().getVariableDataResponse().getDataRecords().size() <= 1) {
            // unplausibly low number of records
            logger.trace("WMBusKamstrupMultiCal302Handler: checkMessage(): malformed message: record count <= 1");
            return false;
        }

        // check for date in the past
        record = findRecord(new byte[] { 0x42 }, new byte[] { 0x6c });
        if (record == null) {
            // date is missing
            logger.trace("WMBusKamstrupMultiCal302Handler: checkMessage(): malformed message: previous measurement date missing");
            return false;
        } else if (record.getDataValueType() == DataValueType.DATE) {
            Date date = (java.util.Date) record.getDataValue();
            Calendar calNow = Calendar.getInstance(); // now, here
            Calendar calPrevious = Calendar.getInstance();
            calPrevious.setTime(date);
            if (!calPrevious.before(calNow)) {
                // previous date > now
                logger.trace("WMBusKamstrupMultiCal302Handler: checkMessage(): malformed message: previous measurement date in the future");
                return false;
            }
        }

        // check for negative values in the Wh records
        // current
        record = findRecord(new byte[] { 0x03 }, new byte[] { 0x06 });
        if (record == null) {
            // Wh value missing
            logger.trace("WMBusKamstrupMultiCal302Handler: checkMessage(): malformed message: current Wh value missing");
            return false;
        } else {
            Double currentWh = record.getScaledDataValue();
            if (currentWh < 0) {
                // negative Wh value
                logger.trace("WMBusKamstrupMultiCal302Handler: checkMessage(): malformed message: current Wh value negative");
                return false;
            }
        }
        // previous
        record = findRecord(new byte[] { 0x43 }, new byte[] { 0x06 });
        if (record == null) {
            // Wh value missing
            logger.trace("WMBusKamstrupMultiCal302Handler: checkMessage(): malformed message: previous Wh value missing");
            return false;
        } else {
            Double currentWh = record.getScaledDataValue();
            if (currentWh < 0) {
                // negative Wh value
                logger.trace("WMBusKamstrupMultiCal302Handler: checkMessage(): malformed message: previous Wh value negative");
                return false;
            }
        }

        // passed all checks
        return true;
    }
}
