/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem;

import static org.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.device.techem.Record.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Subset of WMBus constants specific to Techem devices.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public interface TechemBindingConstants {

    String _68TCH116255662 = "68TCH116255662"; // warm
    String _68TCH1162552272 = "68TCH1162552272"; // cold
    String _68TCH116255443 = "68TCH116255443"; // heat
    String _68TCH118255818 = "68TCH118255818"; // hkv 118
    String _68TCH100255861 = "68TCH100255861"; // hkv 61
    String _68TCH100255864 = "68TCH100255864"; // khv 64
    String _68TCH105255869 = "68TCH105255869"; // kkv 69

    // techem heat cost allocators (Heizkostenverteiler)
    String THING_TYPE_NAME_TECHEM_HKV118 = "techem_hkv118";
    String THING_TYPE_NAME_TECHEM_HKV61 = "techem_hkv61";
    String THING_TYPE_NAME_TECHEM_HKV64 = "techem_hkv64";
    String THING_TYPE_NAME_TECHEM_HKV69 = "techem_hkv69";

    // water meters
    String THING_TYPE_NAME_TECHEM_WARM_WATER_METER = "techem_wz62";
    String THING_TYPE_NAME_TECHEM_COLD_WATER_METER = "techem_wz72";
    // heat meter
    String THING_TYPE_NAME_TECHEM_HEAT_METER = "techem_wz43";

    ThingTypeUID THING_TYPE_TECHEM_HKV118 = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_HKV118);
    ThingTypeUID THING_TYPE_TECHEM_HKV61 = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_HKV61);
    ThingTypeUID THING_TYPE_TECHEM_HKV64 = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_HKV64);
    ThingTypeUID THING_TYPE_TECHEM_HKV69 = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_HKV69);
    ThingTypeUID THING_TYPE_TECHEM_WARM_WATER_METER = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_WARM_WATER_METER);
    ThingTypeUID THING_TYPE_TECHEM_COLD_WATER_METER = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_COLD_WATER_METER);
    ThingTypeUID THING_TYPE_TECHEM_HEAT_METER = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_HEAT_METER);

    // TODO remove this part once all deployments are migrated
    // old device type, remained here as alias for hkv64
    String THING_TYPE_NAME_TECHEM_HKV = "techem_hkv";
    ThingTypeUID THING_TYPE_TECHEM_HKV = new ThingTypeUID(WMBusBindingConstants.BINDING_ID, THING_TYPE_NAME_TECHEM_HKV);

    Map<String, ThingTypeUID> SUPPORTED_DEVICE_VARIANTS = ImmutableMap.<String, ThingTypeUID> builder()
            .put(_68TCH116255662, THING_TYPE_TECHEM_WARM_WATER_METER) // WZ 62
            .put(_68TCH1162552272, THING_TYPE_TECHEM_COLD_WATER_METER) // WZ 72
            .put(_68TCH116255443, THING_TYPE_TECHEM_HEAT_METER) // WZ 43
            .put(_68TCH118255818, THING_TYPE_TECHEM_HKV118) // HKV 118
            .put(_68TCH100255861, THING_TYPE_TECHEM_HKV61) // HKV 61
            .put(_68TCH100255864, THING_TYPE_TECHEM_HKV64) // HKV 64
            .put(_68TCH105255869, THING_TYPE_TECHEM_HKV69) // HKV 69
            .build();

    Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.copyOf(SUPPORTED_DEVICE_VARIANTS.values());

    Map<String, Type> TECHEM_METER_MAPPING = ImmutableMap.<String, Record.Type> builder()
            .put(CHANNEL_CURRENTREADING, Type.CURRENT_VOLUME) // current value
            .put(CHANNEL_CURRENTDATE, Type.CURRENT_READING_DATE) // present date
            .put(CHANNEL_LASTREADING, Type.PAST_VOLUME) // last billing value
            .put(CHANNEL_LASTDATE, Type.PAST_READING_DATE) // past billing date
            .put(CHANNEL_RECEPTION, Type.RSSI) // past billing date
            .build();

    Map<String, Type> HEAT_ALLOCATOR_MAPPING_69 = ImmutableMap.<String, Record.Type> builder()
            .putAll(TECHEM_METER_MAPPING) // inherit main HKV channel map
            .put(CHANNEL_ROOMTEMPERATURE, Type.ROOM_TEMPERATURE) // room
            .put(CHANNEL_RADIATORTEMPERATURE, Type.RADIATOR_TEMPERATURE) // radiator
            .build();

    // channel mapping for thing types
    Map<ThingTypeUID, Map<String, Type>> RECORD_MAP = ImmutableMap.<ThingTypeUID, Map<String, Type>> builder()
            .put(THING_TYPE_TECHEM_HKV118, TECHEM_METER_MAPPING) // basic HKV mapping
            .put(THING_TYPE_TECHEM_HKV61, TECHEM_METER_MAPPING) // basic HKV mapping
            .put(THING_TYPE_TECHEM_HKV64, TECHEM_METER_MAPPING) // again basic HKV mapping
            .put(THING_TYPE_TECHEM_HKV69, HEAT_ALLOCATOR_MAPPING_69) // here we have two temperature channels
            .put(THING_TYPE_TECHEM_WARM_WATER_METER, TECHEM_METER_MAPPING) // warm
            .put(THING_TYPE_TECHEM_COLD_WATER_METER, TECHEM_METER_MAPPING) // cold
            .put(THING_TYPE_TECHEM_HEAT_METER, ImmutableMap.of()) // head

            .build();

}
