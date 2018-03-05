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

// Thing resp. device handler for the Qundis Qheat 5 water heat meter (Wärmemengenzähler)

public class WMBusQundisQHeatHandler extends BaseThingHandler implements WMBusMessageListener {

    // must set this for add new device handlers
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_QUNDIS_QHEAT_5);
    private final Logger logger = LoggerFactory.getLogger(WMBusQundisQHeatHandler.class);
    private String deviceId;
    private WMBusBridgeHandler bridgeHandler;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private WMBusDevice techemDevice;

    public WMBusQundisQHeatHandler(Thing thing) {
        super(thing);
        logger.debug("WMBusQundisQHeatHandler: new() for Thing" + thing.toString());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WMBusQundisQHeatHandler handler.");
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
        logger.debug("Disposing WMBusQundisQHeatHandler handler.");
    }

    // gets affected from onChangedWMBusDevice()
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("WMBusQundisQHeatHandler: handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: " + command.toString());
        if (command == RefreshType.REFRESH) {
            logger.trace("WMBusQundisQHeatHandler: handleCommand(): (2/5) command.refreshtype == REFRESH");
            State newState = UnDefType.NULL;

            //TODO generisch machen über abgeleitete Handler oder über 1 generischen Handler mit gerätespezifischen WMBusDevice-Ableitungsklassen?
            //TODO manche VIB-DIB-Kombinationen wiederholen sich -> siehe JMbus-Bibliothek-Dokumentation betreffend DataRecord, wies das kodiert ist
            // -> ev. Verallgemeinerung auf 1 Klasse je Typ möglich (WATER_METER, HEAT_COST_ALLOCATOR, HEAT_METER usw.)

            if (techemDevice != null) {
                logger.trace("WMBusQundisQHeatHandler: handleCommand(): (3/5) deviceMessage != null");
                //TODO make own channel type for energy_Wh
                /*
                 * DIB:0x04, VIB:0x6d -> descr:DATE_TIME, function:INST_VAL, value:Wed Nov 15 17:49:00 CET 2017 -- current reading (timestamp)
                 * DIB:0x0c, VIB:0x05 -> descr:ENERGY, function:INST_VAL, scaled value:0.0, unit:WATT_HOUR, Wh -- current reading (measurement)
                 * DIB:0x0d, VIB:0xff 0x5f -> descr:MANUFACTURER_SPECIFIC, function:INST_VAL, value:*?,??4? -- currently not interpreted; also used during installation messages
                 * DIB:0x02, VIB:0xfd 0x17 -> descr:ERROR_FLAGS, function:INST_VAL, value:0 -- numeric (?) value of current error
                 * DIB:0x0c, VIB:0x78 -> descr:FABRICATION_NO, function:INST_VAL, value:56725702 -- currently not interpreted; is not equal to the WMBus device ID
                 */
                switch (channelUID.getId()) {
                    case CHANNEL_RECEPTION: {
                        logger.trace("WMBusQundisQHeatHandler: handleCommand(): (4/5): got a valid channel: RECEPTION");
                        newState = new DecimalType(techemDevice.getOriginalMessage().getRssi());
                        break;
                    }
                    //TODO put all the date value conversions into an helper method
                    //TODO do not instantiate new byte arrays each time (re-use / convert to constants)
                    case CHANNEL_CURRENTREADING: {
                        logger.trace("WMBusQundisQHeatHandler: handleCommand(): (4/5): got a valid channel: CURRENTREADING");
                        DataRecord record = findRecord(new byte[] { 0x0c }, new byte[] { 0x05 });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("WMBusQundisQHeatHandler: handleCommand(): record not found in message");
                        }
                        break;
                    }
                    case CHANNEL_CURRENTDATE: {
                        logger.trace("WMBusQundisQHeatHandler: handleCommand(): (4/5): got a valid channel: CURRENTDATE");
                        DataRecord record = findRecord(new byte[] { 0x04 }, new byte[] { 0x6d }); //TODO same as for Qcaloric 5,5 and Qwater 5,5
                        if (record != null && record.getDataValueType() == DataValueType.DATE) {
                            Date date = (java.util.Date) record.getDataValue();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            newState = new DateTimeType(cal);
                        } else {
                            logger.trace("WMBusQundisQHeatHandler: handleCommand(): record not found in message or not of type date");
                        }
                        break;
                    }
                    case CHANNEL_ERRORFLAGS: { // this is a current value / inst_val
                        logger.trace("WMBusQundisQHeatHandler: handleCommand(): (4/5): got a valid channel: ERRORFLAGS");
                        DataRecord record = findRecord(new byte[] { 0x02 }, new byte[] { (byte) 0xFD, 0x17 });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("WMBusQundisQHeatHandler: handleCommand(): record not found in message");
                        }
                        break;
                    }
                    default:
                        logger.debug("WMBusQundisQHeatHandler: handleCommand(): (4/5): no channel to put this value into found: " + channelUID.getId());
                        break;
                }
                logger.trace("WMBusQundisQHeatHandler: handleCommand(): (5/5) assigning new state to channel '" + channelUID.getId().toString() + "': " + newState.toString());
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
        logger.trace("WMBusQundisQHeatHandler: getBridgeHandler() begin");
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
        logger.trace("WMBusQundisQHeatHandler: getBridgeHandler() returning bridgehandler");
        return this.bridgeHandler;
    }

    private WMBusDevice getDevice() {
        logger.trace("WMBusQundisQHeatHandler: getDevice() begin");
        WMBusBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("thinghandler: getDevice() end: returning null");
            return null;
        }
        logger.trace("WMBusQundisQHeatHandler: getDevice() end: returning devicebyid");
        return bridgeHandler.getDeviceById(deviceId);
    }

    // entry point - gets device here
    @Override
    public void onNewWMBusDevice(WMBusDevice wmBusDevice) {
        logger.trace("WMBusQundisQHeatHandler: onNewWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceId().equals(deviceId)) {
            logger.trace("WMBusQundisQHeatHandler: onNewWMBusDevice(): yes it's me");
            logger.trace("WMBusQundisQHeatHandler: onNewWMBusDevice(): updating status to online");
            updateStatus(ThingStatus.ONLINE);
            logger.trace("WMBusQundisQHeatHandler: onNewWMBusDevice(): calling onChangedWMBusDevice()");
            onChangedWMBusDevice(wmBusDevice);
        }
        logger.trace("WMBusQundisQHeatHandler: onNewWMBusDevice(): no");
    }

    @Override
    public void onChangedWMBusDevice(WMBusDevice wmBusDevice) {
        logger.trace("WMBusQundisQHeatHandler: onChangedWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceId().equals(deviceId)) {
            techemDevice = wmBusDevice;
            logger.trace("WMBusQundisQHeatHandler: onChangedWMBusDevice(): yes");
            // in between the standard OMS format messages, this device sends out messages in manufcaturer-specific format in between -> filter these out
            // these messages are missing the usual values -> would lead to channels being set to NULL
            /*  TODO check does this device also send these manufacturer-specific messages?
             * DIB:0D, VIB:FF5F -> descr:MANUFACTURER_SPECIFIC, function:INST_VAL, value:/
             * DIB:04, VIB:6D -> descr:DATE_TIME, function:INST_VAL, value:Mon Mar 05 12:33:00 CET 2018
             * (no other records in these messages)
             */
            //TODO why is type cast at 0xff required? 0xff already is a byte, right?
            DataRecord record = findRecord(new byte[] { 0x0d }, new byte[] { (byte) 0xff, 0x5f });
            if (record != null) {
                logger.trace("WMBusQundisQHeatHandler: onChangedWMBusDevice(): this is a message with non-OMS manufacturer-specific value, ignoring this message");
            } else {
                // refresh all channels -> handleCommand()
                logger.trace("WMBusQundisQHeatHandler: onChangedWMBusDevice(): inform all channels to refresh");
                for (Channel curChan : getThing().getChannels()) {
                    handleCommand(curChan.getUID(), RefreshType.REFRESH);
                }
            }
        } else {
            logger.trace("WMBusQundisQHeatHandler: onChangedWMBusDevice(): no");
        }
        logger.trace("WMBusQundisQHeatHandler: onChangedWMBusDevice(): return");
    }
}
