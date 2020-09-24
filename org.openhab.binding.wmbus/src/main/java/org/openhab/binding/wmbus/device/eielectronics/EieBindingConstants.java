package org.openhab.binding.wmbus.device.eielectronics;

import com.google.common.collect.ImmutableSet;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.wmbus.WMBusBindingConstants;

import java.util.Set;

public interface EieBindingConstants {

    String EIE_SMOKE_DETECTOR = "eie_smoke_detector";

    ThingTypeUID THING_TYPE_EIE_SMOKE_DETECTOR = new ThingTypeUID(WMBusBindingConstants.BINDING_ID, EIE_SMOKE_DETECTOR);

    Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.of(THING_TYPE_EIE_SMOKE_DETECTOR);

    String EIE_MANUFACTURER_ID = "EIE";

    String CHANNEL_SOFTWARE_VERSION = "software_version";

    String CHANNEL_CURRENT_DATE = "current_date";
    String CHANNEL_CURRENT_DATE_STRING = "current_date_string";
    String CHANNEL_CURRENT_DATE_NUMBER = "current_date_number";

    String CHANNEL_HEAD_STATUS_DUST_LEVEL = "head_status_dust_level";
    String CHANNEL_HEAD_STATUS_SOUNDER_FAULT_FLAG = "head_status_sounder_fault_flag";
    String CHANNEL_HEAD_STATUS_HEAD_TAMPER_FLAG = "head_status_head_tamper_flag";
    String CHANNEL_HEAD_STATUS_EOF_REACHED_FLAG = "head_status_eof_reached_flag";
    String CHANNEL_HEAD_STATUS_BATTERY_VOLTAGE = "head_status_battery_voltage";
    String CHANNEL_HEAD_STATUS_LOW_BATTERY_FAULT = "head_status_low_battery_fault";
    String CHANNEL_HEAD_STATUS_ALARM_SENSOR_FAULT = "head_status_alarm_sensor_fault";
    String CHANNEL_HEAD_STATUS_OBSTACLE_DETECTION_FAULT = "head_status_obstacle_detection_fault";
    String CHANNEL_HEAD_STATUS_EOF_WITHIN_12_MONTHS_FLAG = "head_status_eof_within_12_months_flag";
    String CHANNEL_HEAD_STATUS_SEODS_INSTALLATION_COMPLETE = "head_status_seods_installation_complete";
    String CHANNEL_HEAD_STATUS_ENVIRONMENT_CHANGED_SINCE_LAST_INSTALLATION_FLAG = "head_status_environment_changed_since_last_installation_flag";
    String CHANNEL_HEAD_STATUS_COMMUNICATION_TO_HEAD_FAULT_FLAG = "head_status_communication_to_head_fault_flag";
    String CHANNEL_HEAD_STATUS_OBSTACLE_DETECTION_NOT_POSSIBLE_DUE_TO_INTERFERENCE_FLAG =
            "head_status_obstacle_detection_not_possible_due_to_interference_flag";
    String CHANNEL_HEAD_STATUS_DISTANCE_TO_FIXED_OBSTACLE_AT_LAST_INSTALLATION = "head_status_distance_to_fixed_obstacle_at_last_installation";
    String CHANNEL_HEAD_STATUS_OBSTACLE_DETECTED = "head_status_obstacle_detected";
    String CHANNEL_HEAD_STATUS_SMOKE_ALARM_COVERING_DETECTED = "head_status_smoke_alarm_covering_detected";

    String CHANNEL_ERROR_FLAGS = "error_flags";
    String CHANNEL_COMMISSIONING_DATE = "commissioning_date";
    String CHANNEL_COMMISSIONING_DATE_STRING = "commissioning_date_string";
    String CHANNEL_COMMISSIONING_DATE_NUMBER = "commissioning_date_number";
    String CHANNEL_LAST_SOUND_TEST_DATE = "last_sound_test_date";
    String CHANNEL_LAST_SOUND_TEST_DATE_STRING = "last_sound_test_date_string";
    String CHANNEL_LAST_SOUND_TEST_DATE_NUMBER = "last_sound_test_date_number";
    String CHANNEL_ALARM_COUNTER = "alarm_counter";
    String CHANNEL_ALARM_LAST_DATE = "alarm_last_date";
    String CHANNEL_ALARM_LAST_DATE_STRING = "alarm_last_date_string";
    String CHANNEL_ALARM_LAST_DATE_NUMBER = "alarm_last_date_number";
    String CHANNEL_REMOVE_COUNTER = "remove_counter";
    String CHANNEL_REMOVE_DURATION = "remove_duration";
    String CHANNEL_REMOVE_LAST_DATE = "remove_last_date";
    String CHANNEL_REMOVE_LAST_DATE_STRING = "remove_last_date_string";
    String CHANNEL_REMOVE_LAST_DATE_NUMBER = "remove_last_date_number";
    String CHANNEL_TEST_BUTTON_COUNTER = "test_button_counter";
    String CHANNEL_TEST_BUTTON_LAST_DATE = "test_button_last_date";
    String CHANNEL_TEST_BUTTON_LAST_DATE_STRING = "test_button_last_date_string";
    String CHANNEL_TEST_BUTTON_LAST_DATE_NUMBER = "test_button_last_date_number";
}

