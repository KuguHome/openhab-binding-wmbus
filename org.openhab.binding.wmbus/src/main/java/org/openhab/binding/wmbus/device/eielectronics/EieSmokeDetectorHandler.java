package org.openhab.binding.wmbus.device.eielectronics;

import java.util.Date;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.generic.GenericWMBusThingHandler;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DataRecord.DataValueType;
import org.openmuc.jmbus.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EieSmokeDetectorHandler extends GenericWMBusThingHandler<WMBusDevice> {

    // -> descr:OTHER_SOFTWARE_VERSION, function:INST_VAL, value:010106"
    public static final RecordType SOFTWARE_VERSION_0B_FD0F = new RecordType(0x0B,new byte[] {(byte) 0xFD, 0x0F});
    // -> descr:DATE_TIME, function:INST_VAL, value:Tue Sep 15 07:19:00 CEST 2020"
    public static final RecordType DATE_TIME_04_6D = new RecordType(0x04,0x6D);
    // -> descr:ERROR_FLAGS, function:INST_VAL, value:0"
    public static final RecordType ERROR_FLAGS_02_FD17 = new RecordType(0x02,new byte[] {(byte) 0xFD, 0x17});
    // -> descr:DATE, function:INST_VAL, tariff:2, value:Fri Jun 12 00:00:00 CEST 2020"
    public static final RecordType DATE_8220_6C = new RecordType(new byte[] {(byte) 0x82, 0x20},0x6C);
    // -> descr:DATE, function:INST_VAL, storage:1, value:Sun Aug 16 00:00:00 CEST 2020"
    public static final RecordType DATE_42_6C = new RecordType(0x42,0x6C);
    // -> descr:MANUFACTURER_SPECIFIC, function:INST_VAL, subunit:1, value:6360832"
    public static final RecordType SMOKE_DETECTOR_HEAD_STATUS_8440_FF2C = new RecordType(new byte[] {(byte) 0x84, 0x40}, new byte[] {(byte) 0xFF, 0x2C});
    // -> descr:CUMULATION_COUNTER, function:INST_VAL, tariff:1, subunit:1, value:0"
    public static final RecordType CUMULATION_COUNTER_8250_FD61 = new RecordType(new byte[] {(byte) 0x82, 0x50},new byte[] {(byte) 0xFD, 0x61});
    // -> descr:DATE, function:INST_VAL, tariff:1, subunit:1, value:Sat Jan 01 00:00:00 CET 2000"
    public static final RecordType DATE_8250_6C = new RecordType(new byte[] {(byte) 0x82, 0x50}, 0x6C);
    // -> descr:CUMULATION_COUNTER, function:INST_VAL, tariff:2, subunit:1, value:0"
    public static final RecordType CUMULATION_COUNTER_8260_FD61 = new RecordType(new byte[] {(byte) 0x82, 0x60},new byte[] {(byte) 0xFD, 0x61});
    // -> descr:TARIF_DURATION, function:INST_VAL, tariff:2, subunit:1, value:0, unit:MIN, min"
    public static final RecordType TARIF_DURATION_8360_FD31 = new RecordType(new byte[] {(byte) 0x83, 0x60}, new byte[] {(byte) 0xFD, 0x31});
    // -> descr:DATE, function:INST_VAL, tariff:2, subunit:1, value:Sat Jan 01 00:00:00 CET 2000"
    public static final RecordType DATE_8260_6C = new RecordType(new byte[] {(byte) 0x82, 0x60}, 0x6C);
    // -> descr:CUMULATION_COUNTER, function:INST_VAL, tariff:3, subunit:1, value:5"
    public static final RecordType CUMULATION_COUNTER_8270_FD61 = new RecordType(new byte[] {(byte) 0x82, 0x70}, new byte[] {(byte) 0xFD, 0x61});
    // -> descr:DATE, function:INST_VAL, tariff:3, subunit:1, value:Mon Jul 20 00:00:00 CEST 2020"
    public static final RecordType DATE_8270_6C = new RecordType(new byte[] {(byte) 0x82, 0x70}, 0x6C);

    private final Logger logger = LoggerFactory.getLogger(EieSmokeDetectorHandler.class);
    private Map<String, Object> parsedFrame = new HashMap<>();

    public EieSmokeDetectorHandler(Thing thing, KeyStorage keyStorage, UnitRegistry unitRegistry) {
        super(thing, keyStorage, unitRegistry, Collections.emptyMap());
    }

    @Override
    protected WMBusDevice parseDevice(WMBusDevice device) throws DecodingException {
        WMBusDevice parsedDevice = super.parseDevice(device);

        DataRecord record;
        if ((record = parsedDevice.findRecord(SOFTWARE_VERSION_0B_FD0F)) != null) {
            parsedFrame.put(EieBindingConstants.CHANNEL_SOFTWARE_VERSION, record.getDataValue());
        }
        if ((record = parsedDevice.findRecord(DATE_TIME_04_6D)) != null) {
            Date date = parseDateTime(record);
            parsedFrame.put(EieBindingConstants.CHANNEL_CURRENT_DATE, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_CURRENT_DATE_NUMBER, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_CURRENT_DATE_STRING, date);
        }
        if ((record = parsedDevice.findRecord(ERROR_FLAGS_02_FD17)) != null) {
            parsedFrame.put(EieBindingConstants.CHANNEL_ERROR_FLAGS, record.getDataValue());
        }
        if ((record = parsedDevice.findRecord(DATE_8220_6C)) != null) {
            Date date = parseDate(record);
            parsedFrame.put(EieBindingConstants.CHANNEL_COMMISSIONING_DATE, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_COMMISSIONING_DATE_NUMBER, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_COMMISSIONING_DATE_STRING, date);
        }
        if ((record = parsedDevice.findRecord(DATE_42_6C)) != null) {
            Date date = parseDate(record);
            parsedFrame.put(EieBindingConstants.CHANNEL_LAST_SOUND_TEST_DATE, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_LAST_SOUND_TEST_DATE_NUMBER, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_LAST_SOUND_TEST_DATE_STRING, date);
        }
        if ((record = parsedDevice.findRecord(CUMULATION_COUNTER_8250_FD61)) != null) {
            parsedFrame.put(EieBindingConstants.CHANNEL_ALARM_COUNTER, record.getDataValue());
        }
        if ((record = parsedDevice.findRecord(DATE_8250_6C)) != null) {
            Date date = parseDate(record);
            parsedFrame.put(EieBindingConstants.CHANNEL_ALARM_LAST_DATE, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_ALARM_LAST_DATE_NUMBER, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_ALARM_LAST_DATE_STRING, date);
        }
        if ((record = parsedDevice.findRecord(CUMULATION_COUNTER_8260_FD61)) != null) {
            parsedFrame.put(EieBindingConstants.CHANNEL_REMOVE_COUNTER, record.getDataValue());
        }
        if ((record = parsedDevice.findRecord(TARIF_DURATION_8360_FD31)) != null) {
            parsedFrame.put(EieBindingConstants.CHANNEL_REMOVE_DURATION, record.getDataValue());
        }
        if ((record = parsedDevice.findRecord(DATE_8260_6C)) != null) {
            Date date = parseDate(record);
            parsedFrame.put(EieBindingConstants.CHANNEL_REMOVE_LAST_DATE, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_REMOVE_LAST_DATE_NUMBER, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_REMOVE_LAST_DATE_STRING, date);
        }
        if ((record = parsedDevice.findRecord(CUMULATION_COUNTER_8270_FD61)) != null) {
            parsedFrame.put(EieBindingConstants.CHANNEL_TEST_BUTTON_COUNTER, record.getDataValue());
        }
        if ((record = parsedDevice.findRecord(DATE_8270_6C)) != null) {
            Date date = parseDate(record);
            parsedFrame.put(EieBindingConstants.CHANNEL_TEST_BUTTON_LAST_DATE, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_TEST_BUTTON_LAST_DATE_NUMBER, date);
            parsedFrame.put(EieBindingConstants.CHANNEL_TEST_BUTTON_LAST_DATE_STRING, date);
        }

        record = device.findRecord(SMOKE_DETECTOR_HEAD_STATUS_8440_FF2C);
        if (record != null && record.getDataValueType() == DataRecord.DataValueType.LONG &&
                record.getDescription() == DataRecord.Description.MANUFACTURER_SPECIFIC) {
            // note .getRawData requires patch to jmbus!
            EieHeadStatusDataParser headStatus = new EieHeadStatusDataParser(record.getRawData());

            // first byte
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_DUST_LEVEL, headStatus.getDustLevel());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_SOUNDER_FAULT_FLAG, headStatus.isSounderFault());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_HEAD_TAMPER_FLAG, headStatus.isAlarmRemoved());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_EOF_REACHED_FLAG, headStatus.isEOLReached());

            // second byte
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_BATTERY_VOLTAGE, headStatus.getBatteryVoltageLevel());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_LOW_BATTERY_FAULT, headStatus.isLowBattery());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_ALARM_SENSOR_FAULT, headStatus.isAlarmSensorFault());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_OBSTACLE_DETECTION_FAULT, headStatus.isObstacleDetectionFault());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_EOF_WITHIN_12_MONTHS_FLAG, headStatus.isEOL12Months());

            // third byte
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_SEODS_INSTALLATION_COMPLETE, headStatus.isSEODSInstallationComplete());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_ENVIRONMENT_CHANGED_SINCE_LAST_INSTALLATION_FLAG, headStatus.isEnvironmentChanged());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_COMMUNICATION_TO_HEAD_FAULT_FLAG, headStatus.isCommunicationToHeadFault());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_OBSTACLE_DETECTION_NOT_POSSIBLE_DUE_TO_INTERFERENCE_FLAG, headStatus.isObstacleDetectionInterferenced());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_DISTANCE_TO_FIXED_OBSTACLE_AT_LAST_INSTALLATION, headStatus.getDistance());

            // fourth byte
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_OBSTACLE_DETECTED, headStatus.isObstacleDetected());
            parsedFrame.put(EieBindingConstants.CHANNEL_HEAD_STATUS_SMOKE_ALARM_COVERING_DETECTED, headStatus.isSmokeAlarmCoveringDetected());
        }

        return parsedDevice;
    }

    private Date parseDate(DataRecord record) {
        if (record.getDataValueType() == DataValueType.DATE && record.getDataValue() instanceof Date) {
            return (Date) record.getDataValue();
        }
        return null;
    }

    private Date parseDateTime(DataRecord record) {
        if (record.getDataValueType() == DataValueType.DATE && record.getDataValue() instanceof Date) {
            return (Date) record.getDataValue();
        }
        return null;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (parsedFrame.containsKey(channelUID.getId())) {
            // channel directly maps to manufacturer data appended to frame
            logger.debug("Mapping custom smoke detector channel {} to manufacturer data", channelUID);

            Object value = parsedFrame.get(channelUID.getId());
            if (value == null) {
                updateState(channelUID, UnDefType.NULL);
            } else if (value instanceof LocalDateTime) {
                updateState(channelUID, convertDate(value));
            } else if (value instanceof Date) {
                updateState(channelUID, convertDate(value));
            } else if (value instanceof Number) {
                updateState(channelUID, new DecimalType(((Number) value).floatValue()));
            } else if (value instanceof Boolean) {
                updateState(channelUID, ((boolean) value) ? OnOffType.ON : OnOffType.OFF);
            } else {
                logger.warn("Unsupported value type {}", value);
            }
        } else {
            // try to do a lookup based on channel to record mapping
            super.handleCommand(channelUID, command);
        }
    }



}
