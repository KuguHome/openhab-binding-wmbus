package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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

import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.RecordType;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.TechemHKV;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusDevice;

// TODO generisch machen über abgeleitete Handler oder über 1 generischen Handler mit gerätespezifischen WMBusDevice-Ableitungsklassen?

// Device/thing handler for the Kamstrup MultiCal 302 heat meater (Wärmezähler)
public class WMBusKamstrupMultiCal302Handler extends WMBusDeviceHandler {

	// set this to add new device handlers
	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_KAMSTRUP_MULTICAL_302);
	private final Logger logger = LoggerFactory.getLogger(WMBusKamstrupMultiCal302Handler.class);
	private String deviceId;
	private WMBusBridgeHandler bridgeHandler;

	/*
	 * DIB:03, VIB:06 -> descr:ENERGY, function:INST_VAL, scaled value:0.0, unit:WATT_HOUR, Wh -- current energy total reading in Wh
	 * DIB:43, VIB:06 -> descr:ENERGY, function:INST_VAL, storage:1, scaled value:0.0, unit:WATT_HOUR, Wh -- previous energy total reading in Wh
	 * DIB:03, VIB:14 -> descr:VOLUME, function:INST_VAL, scaled value:0.0, unit:CUBIC_METRE, m³ -- current water volume total reading in m³
	 * DIB:42, VIB:6C -> descr:DATE, function:INST_VAL, storage:1, value:Thu Nov 30 00:00:00 CET 2017  -- previous reading timestamp
	 * DIB:02, VIB:2D -> descr:POWER, function:INST_VAL, scaled value:0.0, unit:WATT, W -- current water heat power reading in W
	 * DIB:01, VIB:FF21 -> descr:MANUFACTURER_SPECIFIC, function:INST_VAL, value:16 -- currently not interpreted
	 */

	private static final RecordType TYPE_CURRENT_POWER = new RecordType(0x02, 0x2d);
	private static final RecordType TYPE_CURRENT_ENERGY_TOTAL = new RecordType(0x03, 0x06);
	private static final RecordType TYPE_CURRENT_VOLUME_TOTAL = new RecordType(0x03, 0x14);
	private static final RecordType TYPE_PREVIOUS_ENERGY_TOTAL = new RecordType(0x43, 0x06);
	private static final RecordType TYPE_PREVIOUS_DATE = new RecordType(0x42, 0x6c);
//	private static final RecordType TYPE_MANUFACTURER_SPECIFIC = new RecordType(new byte[] { 0x01 }, new byte[] { (byte) 0xFF, 0x21 });

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private WMBusDevice wmbusDevice;

	public WMBusKamstrupMultiCal302Handler(Thing thing) {
		super(thing);
		logger.debug("new() for Thing" + thing.toString());
	}

	@Override
	public void initialize() {
		logger.debug("Initializing WMBusKamstrupMultiCal302Handler handler.");
		Configuration config = getConfig();
		deviceId = (String) config.getProperties().get(PROPERTY_HKV_ID);
		WMBusDevice device = getDevice();
		if (device instanceof TechemHKV) {
			wmbusDevice = device;
		}
		updateStatus(ThingStatus.ONLINE);
	}

	@Override
	public void dispose() {
		logger.debug("Disposing WMBusKamstrupMultiCal302Handler handler.");
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.trace("handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: " + command.toString());
		if (command == RefreshType.REFRESH) {
			logger.trace("handleCommand(): (2/5) command.refreshtype == REFRESH");
			State newState = UnDefType.NULL;

			if (wmbusDevice != null) {
				logger.trace("handleCommand(): (3/5) deviceMessage != null");
				switch (channelUID.getId()) {
				case CHANNEL_RECEPTION: {
					logger.trace("handleCommand(): (4/5): got a valid channel: RECEPTION");
					newState = new DecimalType(wmbusDevice.getOriginalMessage().getRssi());
					break;
				}
				case CHANNEL_CURRENTPOWER: {
					logger.trace("handleCommand(): (4/5): got a valid channel: CURRENTPOWER");
					DataRecord record = wmbusDevice.findRecord(TYPE_CURRENT_POWER);
					if (record != null) {
						newState = new DecimalType(record.getScaledDataValue());
					} else {
						logger.trace("handleCommand(): record not found in message");
					}
					break;
				}
				case CHANNEL_CURRENTENERGYTOTAL: {
					logger.trace("handleCommand(): (4/5): got a valid channel: CURRENTENERGYTOTAL");
					DataRecord record = wmbusDevice.findRecord(TYPE_CURRENT_ENERGY_TOTAL);
					if (record != null) {
						newState = new DecimalType(record.getScaledDataValue() / 1000); // Wh to kWh (usual value unit)
					} else {
						logger.trace("handleCommand(): record not found in message");
					}
					break;
				}
				case CHANNEL_CURRENTVOLUMETOTAL: {
					logger.trace("handleCommand(): (4/5): got a valid channel: CURRENTVOLUMETOTAL");
					DataRecord record = wmbusDevice.findRecord(TYPE_CURRENT_VOLUME_TOTAL);
					if (record != null) {
						newState = new DecimalType(record.getScaledDataValue());
					} else {
						logger.trace("handleCommand(): record not found in message");
					}
					break;
				}
				case CHANNEL_CURRENTDATE: {
					logger.trace("handleCommand(): (4/5): got a valid channel: CURRENTDATE");
					// only if the main reading is set: set current date to NOW(), since device doesn't publish it
					DataRecord record = wmbusDevice.findRecord(TYPE_PREVIOUS_ENERGY_TOTAL);
					if (record != null) {
						newState = new DateTimeType(ZonedDateTime.now());
					} else {
						logger.trace("handleCommand(): record not found in message or not of type date");
					}
					break;
				}
				case CHANNEL_PREVIOUSENERGYTOTAL: {
					logger.trace("handleCommand(): (4/5): got a valid channel: PREVIOUSENERGYTOTAL");
					DataRecord record = wmbusDevice.findRecord(TYPE_PREVIOUS_ENERGY_TOTAL);
					if (record != null) {
						newState = new DecimalType(record.getScaledDataValue() / 1000); // Wh to kWh (usual value unit)
					} else {
						logger.trace("handleCommand(): record not found in message");
					}
					break;
				}
				case CHANNEL_PREVIOUSDATE: {
					logger.trace("handleCommand(): (4/5): got a valid channel: PREVIOUSDATE");
					DataRecord record = wmbusDevice.findRecord(TYPE_PREVIOUS_DATE);
					if (record != null && record.getDataValueType() == DataValueType.DATE) {
						newState = convertDate(record.getDataValue());
					} else {
						logger.trace("handleCommand(): record not found in message or not of type date");
					}
					break;
				}
				default:
					logger.debug("handleCommand(): (4/5): no channel to put this value into found: " + channelUID.getId());
					break;
				}
				logger.trace("handleCommand(): (5/5) assigning new state to channel '" + channelUID.getId().toString() + "': " + newState.toString());
				updateState(channelUID.getId(), newState);
			}
		}
	}

	protected synchronized WMBusBridgeHandler getBridgeHandler() {
		logger.trace("getBridgeHandler() begin");
		if (bridgeHandler == null) {
			Bridge bridge = getBridge();
			if (bridge == null) {
				return null;
			}
			ThingHandler handler = bridge.getHandler();
			if (handler instanceof WMBusBridgeHandler) {
				bridgeHandler = (WMBusBridgeHandler) handler;
				bridgeHandler.registerWMBusMessageListener(this);
			} else {
				return null;
			}
		}
		logger.trace("getBridgeHandler() returning bridgehandler");
		return bridgeHandler;
	}

	protected WMBusDevice getDevice() {
		logger.trace("getDevice() begin");
		WMBusBridgeHandler bridgeHandler = getBridgeHandler();
		if (bridgeHandler == null) {
			logger.debug("thinghandler: getDevice() end: returning null");
			return null;
		}
		logger.trace("getDevice() end: returning devicebyid");
		return bridgeHandler.getDeviceById(deviceId);
	}

	// entry point - gets device here
	@Override
	public void onNewWMBusDevice(WMBusDevice wmBusDevice) {
		logger.trace("onNewWMBusDevice(): is it me?");
		if (wmBusDevice.getDeviceId().equals(deviceId)) {
			logger.trace("onNewWMBusDevice(): yes it's me");
			logger.trace("onNewWMBusDevice(): updating status to online");
			updateStatus(ThingStatus.ONLINE);
			logger.trace("onNewWMBusDevice(): calling onChangedWMBusDevice()");
			onChangedWMBusDevice(wmBusDevice);
		}
		logger.trace("onNewWMBusDevice(): no");
	}

	@Override
	public void onChangedWMBusDevice(WMBusDevice receivedDevice) {
		logger.trace("onChangedWMBusDevice(): is it me?");
		if (receivedDevice.getDeviceId().equals(deviceId)) {
			logger.trace("onChangedWMBusDevice(): yes");
			// in between the good messages, there are messages with invalid values -> filter these out
			if (!checkMessage(receivedDevice)) {
				logger.trace("onChangedWMBusDevice(): this is a malformed message, ignoring this message");
			} else {
				wmbusDevice = receivedDevice;
				logger.trace("onChangedWMBusDevice(): inform all channels to refresh");
				for (Channel curChan : getThing().getChannels()) {
					handleCommand(curChan.getUID(), RefreshType.REFRESH);
				}
			}
		} else {
			logger.trace("onChangedWMBusDevice(): no");
		}
		logger.trace("onChangedWMBusDevice(): return");
	}

	// in between the good messages, there are messages with invalid values:
	// - previous date in the future
	// - any of the Wh values are negative
	// - number of data records is 0 -> leads to channels being set to NULL
	// --> filter these out
	protected boolean checkMessage(WMBusDevice messageToCheck) {
		DataRecord record;

		// check for number of records being too low to be valid
		if (messageToCheck.getOriginalMessage().getVariableDataResponse().getDataRecords().size() <= 1) {
			// unplausibly low number of records
			logger.trace("checkMessage(): malformed message: record count <= 1");
			return false;
		}

		// check for date in the past
		record = messageToCheck.findRecord(TYPE_PREVIOUS_DATE);
		if (record == null) {
			// date is missing
			logger.trace("checkMessage(): malformed message: previous measurement date missing");
			return false;
		} else if (record.getDataValueType() == DataValueType.DATE) {
			DateTimeType previousDate = convertDate(record.getDataValue());
			if (!previousDate.getZonedDateTime().isBefore(ZonedDateTime.now())) {
				logger.trace("checkMessage(): malformed message: previous measurement date in the future");
				return false;
			}
		}

		// check for negative values in the Wh records
		record = messageToCheck.findRecord(TYPE_CURRENT_ENERGY_TOTAL);
		if (record == null) {
			// Wh value missing
			logger.trace("checkMessage(): malformed message: current Wh value missing");
			return false;
		} else {
			Double currentWh = record.getScaledDataValue();
			if (currentWh < 0) {
				// negative Wh value
				logger.trace("checkMessage(): malformed message: current Wh value negative");
				return false;
			}
		}
		record = messageToCheck.findRecord(TYPE_PREVIOUS_ENERGY_TOTAL);
		if (record == null) {
			// Wh value missing
			logger.trace("checkMessage(): malformed message: previous Wh value missing");
			return false;
		} else {
			Double currentWh = record.getScaledDataValue();
			if (currentWh < 0) {
				// negative Wh value
				logger.trace("checkMessage(): malformed message: previous Wh value negative");
				return false;
			}
		}

		// passed all checks
		return true;
	}

	protected DateTimeType convertDate(Object input) {
		if (input instanceof Date) {
			Date date = (Date) input;
			ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
			zonedDateTime.truncatedTo(ChronoUnit.SECONDS); // throw away millisecond value to avoid, eg. _previous_date changed from 2018-02-28T00:00:00.353+0100 to 2018-02-28T00:00:00.159+0100
			return new DateTimeType(zonedDateTime);
		}
		return null;
	}
}
