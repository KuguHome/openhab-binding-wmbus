/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.wmbus.device.itron;

import java.util.Set;

import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

public interface ItronBindingConstants {

    String ITRON_SMOKE_DETECTOR = "itron_smoke_detector";

    ThingTypeUID THING_TYPE_ITRON_SMOKE_DETECTOR = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            ITRON_SMOKE_DETECTOR);

    Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.of(THING_TYPE_ITRON_SMOKE_DETECTOR);

    String ITRON_MANUFACTURER_ID = "ITW";

    String CHANNEL_CURRENT_DATE = "current_date";
    String CHANNEL_CURRENT_DATE_STRING = "current_date_string";
    String CHANNEL_CURRENT_DATE_NUMBER = "current_date_number";

    String CHANNEL_STATUS_BILLING_DATE = "status_billing_date";
    String CHANNEL_STATUS_REMOVAL_OCCURRED = "status_removal_occurred";
    String CHANNEL_STATUS_PRODUCT_INSTALLED = "status_product_installed";
    String CHANNEL_STATUS_OPERATION_MODE = "status_operation_mode";
    String CHANNEL_STATUS_PERIMETER_INTRUSION_OCCURRED = "status_perimeter_intrusion_occurred";
    String CHANNEL_STATUS_SMOKE_INLET_BLOCKED_OCCURRED = "status_smoke_inlet_blocked_occurred";
    String CHANNEL_STATUS_OUT_OF_TEMP_RANGE_OCCURRED = "status_out_of_temp_range_occurred";
    String CHANNEL_STATUS_PRODUCT_CODE = "status_product_code";
    String CHANNEL_STATUS_BATTERY_LIFETIME = "status_battery_lifetime";
    // String CHANNEL_STATUS_PERIMETER_INTRUSION = "status_perimeter_intrusion";
    // String CHANNEL_STATUS_REMOVAL_ERROR = "status_removal_error";
    // String CHANNEL_STATUS_DATA_ENCRYPTED = "status_data_encrypted";

    String CHANNEL_LAST_SMOKE_ALERT_START_DATE = "last_smoke_alert_start_date";
    String CHANNEL_LAST_SMOKE_ALERT_START_DATE_STRING = "last_smoke_alert_start_date_string";
    String CHANNEL_LAST_SMOKE_ALERT_START_DATE_NUMBER = "last_smoke_alert_start_date_number";
    String CHANNEL_LAST_SMOKE_ALERT_END_DATE = "last_smoke_alert_end_date";
    String CHANNEL_LAST_SMOKE_ALERT_END_DATE_STRING = "last_smoke_alert_end_date_string";
    String CHANNEL_LAST_SMOKE_ALERT_END_DATE_NUMBER = "last_smoke_alert_end_date_number";
    String CHANNEL_LAST_BEEPER_STOPPED_DURING_SMOKE_ALERT_DATE = "last_beeper_stopped_during_smoke_alert_date";
    String CHANNEL_LAST_BEEPER_STOPPED_DURING_SMOKE_ALERT_DATE_STRING = "last_beeper_stopped_during_smoke_alert_date_string";
    String CHANNEL_LAST_BEEPER_STOPPED_DURING_SMOKE_ALERT_DATE_NUMBER = "last_beeper_stopped_during_smoke_alert_date_number";

    String CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_OCCURRED_DATE = "last_perimeter_intrusion_obstacle_occurred_date";
    String CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_OCCURRED_DATE_STRING = "last_perimeter_intrusion_obstacle_occurred_date_string";
    String CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_OCCURRED_DATE_NUMBER = "last_perimeter_intrusion_obstacle_occurred_date_number";
    String CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_REMOVED_DATE = "last_perimeter_intrusion_obstacle_removed_date";
    String CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_REMOVED_DATE_STRING = "last_perimeter_intrusion_obstacle_removed_date_string";
    String CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_REMOVED_DATE_NUMBER = "last_perimeter_intrusion_obstacle_removed_date_number";

    String CHANNEL_LAST_SMOKE_INLET_BLOCKED_DATE = "last_smoke_inlet_blocked_date";
    String CHANNEL_LAST_SMOKE_INLET_BLOCKED_DATE_STRING = "last_smoke_inlet_blocked_date_string";
    String CHANNEL_LAST_SMOKE_INLET_BLOCKED_DATE_NUMBER = "last_smoke_inlet_blocked_date_number";
    String CHANNEL_LAST_SMOKE_INLET_BLOCKING_REMOVED_DATE = "last_smoke_inlet_blocking_removed_date";
    String CHANNEL_LAST_SMOKE_INLET_BLOCKING_REMOVED_DATE_STRING = "last_smoke_inlet_blocking_removed_date_string";
    String CHANNEL_LAST_SMOKE_INLET_BLOCKING_REMOVED_DATE_NUMBER = "last_smoke_inlet_blocking_removed_date_number";

    String CHANNEL_LAST_TEMPERATURE_OUT_OF_RANGE_DATE = "last_temperature_out_of_range_date";
    String CHANNEL_LAST_TEMPERATURE_OUT_OF_RANGE_DATE_STRING = "last_temperature_out_of_range_date_string";
    String CHANNEL_LAST_TEMPERATURE_OUT_OF_RANGE_DATE_NUMBER = "last_temperature_out_of_range_date_number";

    String CHANNEL_LAST_TEST_SWITCH_DATE = "last_test_switch_date";
    String CHANNEL_LAST_TEST_SWITCH_DATE_STRING = "last_test_switch_date_string";
    String CHANNEL_LAST_TEST_SWITCH_DATE_NUMBER = "last_test_switch_date_number";

    String CHANNEL_NUMBER_OF_TEST_SWITCHES_OPERATED = "number_of_test_switches_operated";
    String CHANNEL_PERIMETER_INTRUSION_DAY_COUNTER_CUMULATED = "perimeter_intrusion_day_counter_cumulated";
    String CHANNEL_SMOKE_INLET_DAY_COUNTER_CUMULATED = "smoke_inlet_day_counter_cumulated";
}
