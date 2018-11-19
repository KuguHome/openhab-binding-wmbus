/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.landisgyr;

import java.util.Map;
import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Volume;

import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.WMBusBindingConstants;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import tec.uom.se.unit.ProductUnit;
import tec.uom.se.unit.Units;

/**
 * Subset of WMBus constants specific to Landis+Gyr devicees.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public interface LandisGyrBindingConstants {

    String MANUFACTURER_ID = "LUG";

    // DIB:0C, VIB:06 -> :ENERGY, function:INST_VAL, scaled value:7.8029E7, unit:WATT_HOUR, Wh
    // DIB:0C, VIB:14 -> :VOLUME, function:INST_VAL, scaled value:7226.4400000000005, unit:CUBIC_METRE, m³
    // DIB:0B, VIB:2D -> :POWER, function:INST_VAL, scaled value:33000.0, unit:WATT, W
    // DIB:0B, VIB:3B -> :VOLUME_FLOW, function:INST_VAL, scaled value:1.224, unit:CUBIC_METRE_PER_HOUR, m³/h
    // DIB:0A, VIB:5A -> :FLOW_TEMPERATURE, function:INST_VAL, scaled value:98.60000000000001, unit:DEGREE_CELSIUS, °C
    // DIB:0A, VIB:5E -> :RETURN_TEMPERATURE, function:INST_VAL, scaled value:75.0, unit:DEGREE_CELSIUS, °C
    // DIB:0A, VIB:62 -> :TEMPERATURE_DIFFERENCE, function:INST_VAL, scaled value:23.700000000000003, unit:KELVIN, S
    // DIB:9B10, VIB:2D -> :POWER, function:MAX_VAL, tariff:1, scaled value:52300.0, unit:WATT, W
    // DIB:9B10, VIB:3B -> :VOLUME_FLOW, function:MAX_VAL, tariff:1, scaled value:1.296, unit:CUBIC_METRE_PER_HOUR, m³/h
    // DIB:9A10, VIB:5A -> :FLOW_TEMPERATURE, function:MAX_VAL, tariff:1, scaled value:120.2, unit:DEGREE_CELSIUS, °C
    // DIB:9A10, VIB:5E -> :RETURN_TEMPERATURE, function:MAX_VAL, tariff:1, scaled value:102.2, unit:DEGREE_CELSIUS, °C
    // DIB:3C, VIB:22 -> :ON_TIME, function:ERROR_VAL, value:00000000, unit:HOUR, h
    // DIB:02, VIB:FD17 -> :ERROR_FLAGS, function:INST_VAL, value:0

    // record fields reported by heat meter
    RecordType ENERGY_WATT_HOUR = new RecordType(0x0C, 0x06);
    RecordType VOLUME_CUBIC_METRE = new RecordType(0x0C, 0x14);
    RecordType POWER_WATT = new RecordType(0x0B, 0x2D);
    RecordType FLOW_VOLUME = new RecordType(0x0B, 0x3B);
    RecordType FLOW_TEMPERATURE = new RecordType(0x0A, 0x5A);
    RecordType RETURN_TEMPERATURE = new RecordType(0x0A, 0x5E);
    RecordType TEMPERATURE_DIFFERENCE = new RecordType(0x0A, 0x62);
    RecordType MAX_POWER = new RecordType(new byte[] { (byte) 0x9B, 0x10 }, 0x2D);
    RecordType MAX_FLOW_VOLUME = new RecordType(new byte[] { (byte) 0x9B, 0x10 }, 0x3B);
    RecordType MAX_FLOW_TEMPERATURE = new RecordType(new byte[] { (byte) 0x9A, 0x10 }, 0x5A);
    RecordType MAX_RETURN_TEMPERATURTE = new RecordType(new byte[] { (byte) 0x9A, 0x10 }, 0x5E);
    RecordType ON_TIME_ERROR = new RecordType(0x3C, 0x22);
    RecordType ERROR_FLAGS = new RecordType(0x02, new byte[] { (byte) 0xFD, 0x17 });

    String THING_TYPE_NAME_HEAT_METER = "landis_heat_meter";

    ThingTypeUID THING_TYPE_HEAT_METER = new ThingTypeUID(WMBusBindingConstants.BINDING_ID, THING_TYPE_NAME_HEAT_METER);

    String CHANNEL_ENERGY = "energy";
    String CHANNEL_VOLUME = "volume";
    String CHANNEL_POWER = "power";
    String CHANNEL_FLOW_VOLUME = "flow_volume";
    String CHANNEL_FLOW_TEMPERATURE = "flow_temperature";
    String CHANNEL_RETURN_TEMPERATURE = "return_temperature";
    String CHANNEL_TEMPERATURE_DIFFERENCE = "temperature_difference";
    String CHANNEL_MAX_POWER = "max_power";
    String CHANNEL_MAX_FLOW_VOLUME = "max_flow_volume";
    String CHANNEL_MAX_FLOW_TEMPERATURE = "max_flow_temperature";
    String CHANNEL_MAX_RETURN_TEMPERATURE = "max_return_temperature";
    String CHANNEL_ON_TIME_ERROR = "on_time_error";
    String CHANNEL_ERROR_FLAGS = "error_flags";

    Unit<Volume> CUBIC_METRE_PER_HOUR = new ProductUnit<Volume>(Units.CUBIC_METRE.divide(Units.HOUR));

    // channels supported by heat meter
    Map<String, RecordType> HEAT_METER_CHANNELS = ImmutableMap.<String, RecordType> builder() // quite long list
            .put(CHANNEL_ENERGY, ENERGY_WATT_HOUR) //
            .put(CHANNEL_VOLUME, VOLUME_CUBIC_METRE) //
            .put(CHANNEL_POWER, POWER_WATT) //
            .put(CHANNEL_FLOW_VOLUME, FLOW_VOLUME) //
            .put(CHANNEL_FLOW_TEMPERATURE, FLOW_TEMPERATURE) //
            .put(CHANNEL_RETURN_TEMPERATURE, RETURN_TEMPERATURE) //
            .put(CHANNEL_TEMPERATURE_DIFFERENCE, TEMPERATURE_DIFFERENCE) //
            .put(CHANNEL_MAX_POWER, MAX_POWER) //
            .put(CHANNEL_MAX_FLOW_VOLUME, MAX_FLOW_VOLUME) //
            .put(CHANNEL_MAX_FLOW_TEMPERATURE, MAX_FLOW_TEMPERATURE) //
            .put(CHANNEL_MAX_RETURN_TEMPERATURE, MAX_RETURN_TEMPERATURTE) //
            .put(CHANNEL_ON_TIME_ERROR, ON_TIME_ERROR) //
            .put(CHANNEL_ERROR_FLAGS, ERROR_FLAGS) //
            .build();

    Map<ThingTypeUID, Map<String, RecordType>> RECORD_MAP = ImmutableMap.of(THING_TYPE_HEAT_METER, HEAT_METER_CHANNELS);

    Map<String, ThingTypeUID> SUPPORTED_DEVICE_VARIANTS = ImmutableMap.<String, ThingTypeUID> builder()
            // here we enlist supported devices
            .put("68LUG44", LandisGyrBindingConstants.THING_TYPE_HEAT_METER).build();

    Map<RecordType, Unit<?>> RECORD_UNITS = ImmutableMap.<RecordType, Unit<?>> builder() // break line please!
            .put(ENERGY_WATT_HOUR, SmartHomeUnits.WATT_HOUR) //
            .put(VOLUME_CUBIC_METRE, SIUnits.CUBIC_METRE) //
            .put(POWER_WATT, SIUnits.WATT) //
            // .put(FLOW_VOLUME, CUBIC_METRE_PER_HOUR) //
            .put(FLOW_TEMPERATURE, SIUnits.CELSIUS) //
            .put(RETURN_TEMPERATURE, SIUnits.CELSIUS) //
            .put(TEMPERATURE_DIFFERENCE, SIUnits.KELVIN) //
            .put(MAX_POWER, SIUnits.WATT) //
            // .put(MAX_FLOW_VOLUME, CUBIC_METRE_PER_HOUR) //
            .put(MAX_FLOW_TEMPERATURE, SIUnits.CELSIUS) //
            .put(MAX_RETURN_TEMPERATURTE, SIUnits.CELSIUS) //
            .put(ON_TIME_ERROR, SIUnits.HOUR) //
            // .put(ERROR_FLAGS) //
            .build();

    Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.copyOf(SUPPORTED_DEVICE_VARIANTS.values());

}
