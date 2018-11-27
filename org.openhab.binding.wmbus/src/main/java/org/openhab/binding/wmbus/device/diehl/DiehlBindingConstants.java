/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.diehl;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.WMBusBindingConstants;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Subset of WMBus constants specific to Diehl devicees.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public interface DiehlBindingConstants {

    // Diehl has several identifiers:
    // DME: Diehl Metering
    // DGM: Diehl Gas Metering
    // MDE: Diehl Metering Deutschland
    Set<String> MANUFACTURER_IDS = ImmutableSet.of("DME", "DGM", "MDE");

    // DIB:03, VIB:6E -> HCA, function:INST_VAL, value:0, unit:RESERVED
    // DIB:43, VIB:6E -> HCA, function:INST_VAL, storage:1, value:0, unit:RESERVED
    // DIB:4D, VIB:EE1E -> HCA, function:INST_VAL, storage:1, value:�r, unit:RESERVED
    // DIB:42, VIB:6C -> PREVIOUS_DATE, function:INST_VAL, storage:1, value:Thu Aug 31 00:00:00 CEST 2017
    // DIB:02, VIB:5A -> FLOW_TEMPERATURE, function:INST_VAL, scaled value:17.7, unit: °C
    // DIB:02, VIB:66 -> EXTERNAL_TEMPERATURE, function:INST_VAL, scaled value:17.92, unit: °C
    // DIB:12, VIB:5A -> FLOW_TEMPERATURE, function:MAX_VAL, scaled value:19.23, unit: °C
    // DIB:12, VIB:66 -> EXTERNAL_TEMPERATURE, function:MAX_VAL, scaled value:19.3, unit: °C
    // DIB:D207, VIB:5A -> FLOW_TEMPERATURE, function:MAX_VAL, storage:15, scaled value:25.92, unit: °C
    // DIB:D207, VIB:66 -> EXTERNAL_TEMPERATURE, function:MAX_VAL, storage:15, scaled value:25.3, unit: °C

    // record fields reported by heat meter
    // Ignore - byte values with reserved units, not sure how to decode these and manufacturer documentation is missing
    RecordType HCA_INSTANT = new RecordType(0x03, 0x6E);
    RecordType HCA_STORAGE_1_43 = new RecordType(0x43, 0x6E);
    RecordType HCA_STORAGE_1_4D = new RecordType(0x4D, new byte[] { (byte) 0xEE, 0x1E });
    RecordType PREVIOUS_DATE = new RecordType(0x42, 0x6C);
    RecordType FLOW_TEMPERATURE = new RecordType(0x02, 0x5A);
    RecordType EXTERNAL_TEMPERATURE = new RecordType(0x02, 0x66);
    RecordType MAX_FLOW_TEMPERATURE = new RecordType(0x12, 0x5A);
    RecordType MAX_EXTERNAL_TEMPERATURE = new RecordType(0x12, 0x66);
    RecordType MAX_FLOW_TEMPERATURE_STORAGE_15 = new RecordType(new byte[] { (byte) 0xD2, 0x07 }, 0x5A);
    RecordType MAX_EXTERNAL_TEMPERATURE_STORAGE_15 = new RecordType(new byte[] { (byte) 0xD2, 0x07 }, 0x66);

    String THING_TYPE_NAME_HEAT_COST_ALLOCATOR = "diehl_heat_cost_allocator";

    ThingTypeUID THING_TYPE_HEAT_COST_ALLOCATOR = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_HEAT_COST_ALLOCATOR);

    String CHANNEL_CURRENT_READINGG = "current_reading";
    String CHANNEL_HEAT_COST_ALLOCATION_43 = "heat_cost_allocation_43";
    String CHANNEL_HEAT_COST_ALLOCATION_4D = "heat_cost_allocation_4D";
    String CHANNEL_PREVIOUS_DATE = "previous_date";
    String CHANNEL_FLOW_TEMPERATURE = "flow_temperature";
    String CHANNEL_EXTERNAL_TEMPERATURE = "external_temperature";
    String CHANNEL_MAX_FLOW_TEMPERATURE = "max_flow_temperature";
    String CHANNEL_MAX_EXTERNAL_TEMPERATURE = "max_external_temperature";
    String CHANNEL_MAX_FLOW_TEMPERATURE_STORAGE_15 = "max_flow_temperature15";
    String CHANNEL_MAX_EXTERNAL_TEMPERATURE_STORAGE_15 = "max_external_temperature15";

    // channels supported by heat meter
    Map<String, RecordType> HEAT_METER_CHANNELS = ImmutableMap.<String, RecordType> builder() // quite long list
            .put(CHANNEL_CURRENT_READINGG, HCA_INSTANT) //
            .put(CHANNEL_HEAT_COST_ALLOCATION_43, HCA_STORAGE_1_43) //
            .put(CHANNEL_HEAT_COST_ALLOCATION_4D, HCA_STORAGE_1_4D) //
            .put(CHANNEL_PREVIOUS_DATE, PREVIOUS_DATE) //
            .put(CHANNEL_FLOW_TEMPERATURE, FLOW_TEMPERATURE) //
            .put(CHANNEL_EXTERNAL_TEMPERATURE, EXTERNAL_TEMPERATURE) //
            .put(CHANNEL_MAX_FLOW_TEMPERATURE, MAX_FLOW_TEMPERATURE) //
            .put(CHANNEL_MAX_EXTERNAL_TEMPERATURE, MAX_EXTERNAL_TEMPERATURE) //
            .put(CHANNEL_MAX_FLOW_TEMPERATURE_STORAGE_15, MAX_FLOW_TEMPERATURE_STORAGE_15) //
            .put(CHANNEL_MAX_EXTERNAL_TEMPERATURE_STORAGE_15, MAX_EXTERNAL_TEMPERATURE_STORAGE_15) //

            .build();

    Map<ThingTypeUID, Map<String, RecordType>> RECORD_MAP = ImmutableMap.of(THING_TYPE_HEAT_COST_ALLOCATOR,
            HEAT_METER_CHANNELS);

    Map<String, ThingTypeUID> SUPPORTED_DEVICE_VARIANTS = ImmutableMap.<String, ThingTypeUID> builder()
            // here we enlist supported devices
            .put("68DME1298", DiehlBindingConstants.THING_TYPE_HEAT_COST_ALLOCATOR).build();

    Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.copyOf(SUPPORTED_DEVICE_VARIANTS.values());

}
