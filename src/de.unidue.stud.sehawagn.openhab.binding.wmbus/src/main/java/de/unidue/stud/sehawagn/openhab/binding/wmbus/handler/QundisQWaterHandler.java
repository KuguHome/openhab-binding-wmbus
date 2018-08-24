package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.util.Set;

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

import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusDevice;

//Device/thing handler for the Qundis Qwater 5,5 water flow meter (Wasserzähler)
public class QundisQWaterHandler extends WMBusDeviceHandler {

	// set this when adding new device handler
	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_QUNDIS_QWATER_5_5);
	private final Logger logger = LoggerFactory.getLogger(QundisQWaterHandler.class);

	public QundisQWaterHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.trace("handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: " + command.toString());
		if (command == RefreshType.REFRESH) {
			logger.trace("handleCommand(): (2/5) command.refreshtype == REFRESH");
			State newState = UnDefType.NULL;

			// TODO manche VIB-DIB-Kombinationen wiederholen sich -> siehe JMbus-Bibliothek-Dokumentation betreffend DataRecord, wies das kodiert ist
			// -> ev. Verallgemeinerung auf 1 Klasse je Typ möglich (WATER_METER, HEAT_COST_ALLOCATOR, HEAT_METER usw.)

			if (wmbusDevice != null) {
				logger.trace("handleCommand(): (3/5) deviceMessage != null");
				/*
				 * DIB:0C, VIB:13 -> descr:VOLUME, function:INST_VAL, scaled value:99999.999, unit:CUBIC_METRE, m³ -- current reading / measurement value
				 * DIB:4C, VIB:13 -> descr:VOLUME, function:INST_VAL, storage:1, scaled value:99999.999, unit:CUBIC_METRE, m³ -- reading before previous one over last year
				 * DIB:42, VIB:6C -> descr:DATE, function:INST_VAL, storage:1, value:Sun Dec 31 00:00:00 CET 2017 -- reading before previous one
				 * DIB:CC08, VIB:13 -> descr:VOLUME, function:INST_VAL, storage:17, scaled value:99999.999, unit:CUBIC_METRE, m³  -- previous reading over last month
				 * DIB:C208, VIB:6C -> descr:DATE, function:INST_VAL, storage:17, value:Wed Feb 28 00:00:00 CET 2018  -- previous reading
				 * DIB:02, VIB:BB56 -> descr:VOLUME_FLOW, function:INST_VAL, scaled value:0.0, unit:CUBIC_METRE_PER_HOUR, m³/h -- also a current reading / measurement value
				 * DIB:32, VIB:6C -> descr:DATE, function:ERROR_VAL, value:Wed Mar 31 00:00:00 CEST 2128 -- time of last error
				 * DIB:04, VIB:6D -> descr:DATE_TIME, function:INST_VAL, value:Mon Mar 05 17:13:00 CET 2018 -- timestamp of current / latest reading
				 */
				switch (channelUID.getId()) {
				case CHANNEL_RECEPTION: {
					logger.trace("handleCommand(): (4/5): got a valid channel: RECEPTION");
					newState = new DecimalType(wmbusDevice.getOriginalMessage().getRssi());
					break;
				}
				// TODO do not instantiate new byte arrays each time (re-use / convert to constants)
				case CHANNEL_ERRORDATE: {
					logger.trace("handleCommand(): (4/5): got a valid channel: ERRORDATE");
					DataRecord record = wmbusDevice.findRecord(new byte[] { 0x32 }, new byte[] { 0x6c }); // TODO same as for Qundis Qcaloric 5,5
					if (record != null && record.getDataValueType() == DataValueType.DATE) {
						newState = convertDate(record.getDataValue());
					} else {
						logger.trace("handleCommand(): record not found in message or not of type date");
					}
					break;
				}
				// TODO change current reading to specific channel value (volume_total with proper unit in format)
				case CHANNEL_CURRENTREADING: {
					logger.trace("handleCommand(): (4/5): got a valid channel: CURRENTREADING");
					DataRecord record = wmbusDevice.findRecord(new byte[] { 0x0c }, new byte[] { 0x13 });
					if (record != null) {
						newState = new DecimalType(record.getScaledDataValue());
					} else {
						logger.trace("handleCommand(): record not found in message");
					}
					break;
				}
				case CHANNEL_CURRENTVOLUMEFLOW: {
					logger.trace("handleCommand(): (4/5): got a valid channel: CURRENTVOLUMEFLOW");
					// TODO why is type cast at 0xbb required? 0xbb already is a byte, right?
					DataRecord record = wmbusDevice.findRecord(new byte[] { 0x02 }, new byte[] { (byte) 0xbb, 0x56 });
					if (record != null) {
						newState = new DecimalType(record.getScaledDataValue());
					} else {
						logger.trace("handleCommand(): record not found in message");
					}
					break;
				}
				case CHANNEL_CURRENTDATE: {
					logger.trace("handleCommand(): (4/5): got a valid channel: CURRENTDATE");
					DataRecord record = wmbusDevice.findRecord(new byte[] { 0x04 }, new byte[] { 0x6d }); // TODO same as for Qcaloric 5,5
					if (record != null && record.getDataValueType() == DataValueType.DATE) {
						newState = convertDate(record.getDataValue());
					} else {
						logger.trace("handleCommand(): record not found in message or not of type date");
					}
					break;
				}
				// TODO improve naming: last reading <--> previous reading (need to find out what is actually reported by this device as previous reading)
				case CHANNEL_PREVIOUSREADING: {
					logger.trace("handleCommand(): (4/5): got a valid channel: PREVIOUSREADING");
					// TODO why is type cast at 0xCB required? 0xCB already is a byte, right?
					DataRecord record = wmbusDevice.findRecord(new byte[] { (byte) 0xCC, 0x08 }, new byte[] { 0x13 });
					if (record != null) {
						newState = new DecimalType(record.getScaledDataValue());
					} else {
						logger.trace("handleCommand(): record not found in message");
					}
					break;
				}
				case CHANNEL_PREVIOUSDATE: {
					logger.trace("handleCommand(): (4/5): got a valid channel: PREVIOUSDATE");
					// TODO why is type cast at 0xC2 required? 0xC2 already is a byte, right?
					DataRecord record = wmbusDevice.findRecord(new byte[] { (byte) 0xC2, 0x08 }, new byte[] { 0x6c }); // TODO same as for Qundis Qcaloric 5,5
					if (record != null && record.getDataValueType() == DataValueType.DATE) {
						newState = convertDate(record.getDataValue());
					} else {
						logger.trace("handleCommand(): record not found in message or not of type date");
					}
					break;
				}
				case CHANNEL_LASTREADING: {
					logger.trace("handleCommand(): (4/5): got a valid channel: LASTREADING");
					DataRecord record = wmbusDevice.findRecord(new byte[] { 0x4c }, new byte[] { 0x13 });
					if (record != null) {
						newState = new DecimalType(record.getScaledDataValue());
					} else {
						logger.trace("handleCommand(): record not found in message");
					}
					break;
				}
				case CHANNEL_LASTDATE: {
					logger.trace("handleCommand(): (4/5): got a valid channel: LASTDATE");
					DataRecord record = wmbusDevice.findRecord(new byte[] { 0x42 }, new byte[] { 0x6c }); // TODO same as for Qundis Qcaloric 5,5
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

	@Override
	boolean checkMessage(WMBusDevice receivedDevice) {
		// in between the standard OMS format messages, this device sends out messages in manufcaturer-specific format -> filter these out
		// these messages are missing the usual values -> would lead to channels being set to NULL
		/*
		 * DIB:0D, VIB:FF5F -> descr:MANUFACTURER_SPECIFIC, function:INST_VAL, value:/
		 * DIB:04, VIB:6D -> descr:DATE_TIME, function:INST_VAL, value:Mon Mar 05 12:33:00 CET 2018
		 * (no other records in these messages)
		 */
		// TODO why is type cast at 0xff required? 0xff already is a byte, right?
		DataRecord record = receivedDevice.findRecord(new byte[] { 0x0d }, new byte[] { (byte) 0xff, 0x5f });
		if (record != null) {
			logger.trace("onChangedWMBusDevice(): this is a message with non-OMS manufacturer-specific value, ignoring this message");
			return false;
		}
		return true;
	}
}
