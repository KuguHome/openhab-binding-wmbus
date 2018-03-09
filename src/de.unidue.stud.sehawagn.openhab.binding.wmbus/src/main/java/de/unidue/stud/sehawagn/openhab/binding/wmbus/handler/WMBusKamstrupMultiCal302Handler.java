package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
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
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusDevice;

// Device/thing handler for the Kamstrup MultiCal 302 heat meter (Wärmezähler)
public class WMBusKamstrupMultiCal302Handler extends WMBusDeviceHandler {

	// set this when adding new device handler
	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_KAMSTRUP_MULTICAL_302);
	final Logger logger = LoggerFactory.getLogger(WMBusKamstrupMultiCal302Handler.class);

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

	public WMBusKamstrupMultiCal302Handler(Thing thing) {
		super(thing);
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

	// in between the good messages, there are messages with invalid values:
	// - previous date in the future
	// - any of the Wh values are negative
	// - number of data records is 0 -> leads to channels being set to NULL
	// --> filter these out
	@Override
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
}
