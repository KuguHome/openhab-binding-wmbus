/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.device.techem.Record.Type;
import org.openmuc.jmbus.DeviceType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Subset of WMBus constants specific to Techem devices.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public interface TechemBindingConstants {

    String MANUFACTURER_ID = "TCH";

    String CHANNEL_STATUS = "status";
    String CHANNEL_ALMANAC = "almanac";
    // temperatures
    String CHANNEL_ROOMTEMPERATURE = "room_temperature";
    String CHANNEL_RADIATORTEMPERATURE = "radiator_temperature";
    // values
    String CHANNEL_CURRENTREADING = "current_reading";
    String CHANNEL_LASTREADING = "last_reading";

    // dates
    String CHANNEL_LASTDATE_NUMBER = "last_date_number";
    String CHANNEL_LASTDATE_STRING = "last_date_string";
    String CHANNEL_LASTDATE = "last_date";
    String CHANNEL_CURRENTDATE_NUMBER = "current_date_number";
    String CHANNEL_CURRENTDATE_STRING = "current_date_string";
    String CHANNEL_CURRENTDATE = "current_date";
    String CHANNEL_RECEPTION = "reception";
    String CHANNEL_CURRENTDATE_SMOKE_NUMBER = "current_date_smoke_number";
    String CHANNEL_CURRENTDATE_SMOKE_STRING = "current_date_smoke_string";
    String CHANNEL_CURRENTDATE_SMOKE = "current_date_smoke";

    // warm water version 0x70 -> 112, type 0x62 -> 98
    Variant _68TCH11298_6 = new Variant(0x70, 0x62, 0xA0, DeviceType.WARM_WATER_METER);
    // cold water version 0x70 -> 112, type 0x72 -> 114
    Variant _68TCH112114_16 = new Variant(0x70, 0x72, 0xA0, DeviceType.COLD_WATER_METER);
    // warm water version 0x74 -> 116, type 0x62 -> 98
    Variant _68TCH11698_6 = new Variant(0x74, 0x62, 0xA2, DeviceType.WARM_WATER_METER);
    // cold water version 0x74 -> 116, type 0x72 -> 114
    Variant _68TCH116114_16 = new Variant(0x74, 0x72, 0xA2, DeviceType.COLD_WATER_METER);
    // warm water version 0x95 -> 149, type 0x62 -> 98
    Variant _68TCH14998_6 = new Variant(0x95, 0x62, 0xA2, DeviceType.WARM_WATER_METER);
    // cold water version 0x95 -> 149, type 0x72 -> 114
    Variant _68TCH149114_16 = new Variant(0x95, 0x72, 0xA2, DeviceType.COLD_WATER_METER);

    // heat version 0x22 -> v 34, type 0x43 -> 67
    Variant _68TCH3467_4 = new Variant(0x22, 0x43, 0xA2, DeviceType.HEAT_METER);
    // heat version 0x39 -> v 57, type 0x43 -> 67
    Variant _68TCH5767_4 = new Variant(0x39, 0x43, 0xA2, DeviceType.HEAT_METER);
    // heat version 0x71 -> v 113, type 0x43 -> 67
    Variant _68TCH11367_4_A0 = new Variant(0x71, 0x43, 0xA0, DeviceType.HEAT_METER);
    Variant _68TCH11367_4_A2 = new Variant(0x71, 0x43, 0xA2, DeviceType.HEAT_METER);

    // heat version 0x57 -> v 87, type 0x44 -> 68
    Variant _68TCH8768_4 = new Variant(0x57, 0x44, 0xA2, DeviceType.HEAT_METER);

    // TODO Isn't this a heat meter?
    // hkv version 0x45 -> 69, type 0x43 -> 67
    Variant _68TCH6967_8 = new Variant(0x45, 0x43, 0xA1, DeviceType.HEAT_COST_ALLOCATOR);

    // hkv version 0x61 -> 97, type ?
    Variant _68TCH97255_8 = new Variant(0x61, DeviceType.RESERVED.getId(), 0xA2, DeviceType.HEAT_COST_ALLOCATOR);
    // hkv version 0x64 -> 100, type 0x80 -> 128
    Variant _68TCH100128_8 = new Variant(0x64, 0x80, 0xA0, DeviceType.HEAT_COST_ALLOCATOR);
    // hkv version 0x69 -> 105, type 0x80 -> 128
    Variant _68TCH105128_8 = new Variant(0x69, 0x80, 0xA0, DeviceType.HEAT_COST_ALLOCATOR);
    // hkv version 0x94 -> 148, type 0x80 -> 128
    Variant _68TCH148128_8 = new Variant(0x94, 0x80, 0xA2, DeviceType.HEAT_COST_ALLOCATOR);

    // smoke detector version 0x76 -> 118, type 0xf0 -> 240
    Variant _68TCH118255_161_A0 = new Variant(0x76, 0xf0, 0xA0, DeviceType.SMOKE_DETECTOR);
    Variant _68TCH118255_161_A1 = new Variant(0x76, 0xf0, 0xA1, DeviceType.SMOKE_DETECTOR);

    // water meters
    String THING_TYPE_NAME_TECHEM_WARM_WATER_METER = "techem_wz62";
    String THING_TYPE_NAME_TECHEM_COLD_WATER_METER = "techem_wz72";

    // heat meter
    String THING_TYPE_NAME_TECHEM_HEAT_METER = "techem_wmz43";

    // techem heat cost allocators (Heizkostenverteiler)
    String THING_TYPE_NAME_TECHEM_HKV45 = "techem_hkv45";
    String THING_TYPE_NAME_TECHEM_HKV61 = "techem_hkv61";
    String THING_TYPE_NAME_TECHEM_HKV64 = "techem_hkv64";
    String THING_TYPE_NAME_TECHEM_HKV69 = "techem_hkv69";
    String THING_TYPE_NAME_TECHEM_HKV94 = "techem_hkv94";

    // techem smoke detector
    String THING_TYPE_NAME_TECHEM_SD76 = "techem_sd76";

    ThingTypeUID THING_TYPE_TECHEM_WARM_WATER_METER = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_WARM_WATER_METER);
    ThingTypeUID THING_TYPE_TECHEM_COLD_WATER_METER = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_COLD_WATER_METER);

    ThingTypeUID THING_TYPE_TECHEM_HEAT_METER = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_HEAT_METER);

    ThingTypeUID THING_TYPE_TECHEM_HKV45 = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_HKV45);
    ThingTypeUID THING_TYPE_TECHEM_HKV61 = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_HKV61);
    ThingTypeUID THING_TYPE_TECHEM_HKV64 = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_HKV64);
    ThingTypeUID THING_TYPE_TECHEM_HKV69 = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_HKV69);
    ThingTypeUID THING_TYPE_TECHEM_HKV94 = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_HKV94);

    ThingTypeUID THING_TYPE_TECHEM_SD76 = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_TECHEM_SD76);

    // TODO remove this part once all deployments are migrated
    // old device type, remained here as alias for hkv64
    String THING_TYPE_NAME_TECHEM_HKV = "techem_hkv";
    ThingTypeUID THING_TYPE_TECHEM_HKV = new ThingTypeUID(WMBusBindingConstants.BINDING_ID, THING_TYPE_NAME_TECHEM_HKV);

    Map<Variant, ThingTypeUID> SUPPORTED_DEVICE_VARIANTS = ImmutableMap.<Variant, ThingTypeUID> builder()
            .put(_68TCH11298_6, THING_TYPE_TECHEM_WARM_WATER_METER) // WZ 112_62
            .put(_68TCH112114_16, THING_TYPE_TECHEM_COLD_WATER_METER) // WZ 112_72
            .put(_68TCH11698_6, THING_TYPE_TECHEM_WARM_WATER_METER) // WZ 116_62
            .put(_68TCH116114_16, THING_TYPE_TECHEM_COLD_WATER_METER) // WZ 116_72
            .put(_68TCH14998_6, THING_TYPE_TECHEM_WARM_WATER_METER) // WZ 149_62
            .put(_68TCH149114_16, THING_TYPE_TECHEM_COLD_WATER_METER) // WZ 149_72
            .put(_68TCH3467_4, THING_TYPE_TECHEM_HEAT_METER) // WMZ 34_43
            .put(_68TCH5767_4, THING_TYPE_TECHEM_HEAT_METER) // WMZ 57_43
            .put(_68TCH11367_4_A0, THING_TYPE_TECHEM_HEAT_METER) // WMZ 113_43 with A0 encoding
            .put(_68TCH11367_4_A2, THING_TYPE_TECHEM_HEAT_METER) // WMZ 113_43 with A2 encoding
            .put(_68TCH8768_4, THING_TYPE_TECHEM_HEAT_METER) // WMZ 87_44 with A2 encoding
            .put(_68TCH6967_8, THING_TYPE_TECHEM_HKV45) // HKV 45
            .put(_68TCH97255_8, THING_TYPE_TECHEM_HKV61) // HKV 61
            .put(_68TCH100128_8, THING_TYPE_TECHEM_HKV64) // HKV 64
            .put(_68TCH105128_8, THING_TYPE_TECHEM_HKV69) // HKV 69
            .put(_68TCH148128_8, THING_TYPE_TECHEM_HKV94) // HKV 94
            .put(_68TCH118255_161_A0, THING_TYPE_TECHEM_SD76) // SD 76 with A0 encoding
            .put(_68TCH118255_161_A1, THING_TYPE_TECHEM_SD76) // SD 76 with A1 encoding
            .build();

    Set<String> SUPPORTED_DEVICE_TYPES = ImmutableSet
            .copyOf(SUPPORTED_DEVICE_VARIANTS.keySet().stream().map(Variant::getRawType).collect(Collectors.toSet()));

    Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.copyOf(SUPPORTED_DEVICE_VARIANTS.values());

    // List all channels
    // general channels
    Map<String, Type> TECHEM_METER_MAPPING = ImmutableMap.<String, Record.Type> builder()
            .put(CHANNEL_CURRENTREADING, Type.CURRENT_VOLUME) // current value
            .put(CHANNEL_CURRENTDATE, Type.CURRENT_READING_DATE) // present date
            .put(CHANNEL_CURRENTDATE_STRING, Type.CURRENT_READING_DATE) // present date
            .put(CHANNEL_CURRENTDATE_NUMBER, Type.CURRENT_READING_DATE) // present date
            .put(CHANNEL_LASTREADING, Type.PAST_VOLUME) // last billing value
            .put(CHANNEL_LASTDATE, Type.PAST_READING_DATE) // past billing date
            .put(CHANNEL_LASTDATE_STRING, Type.PAST_READING_DATE) // past billing date
            .put(CHANNEL_LASTDATE_NUMBER, Type.PAST_READING_DATE) // past billing date
            .put(CHANNEL_RECEPTION, Type.RSSI) // Received Signal Strength Indicator
            .put(CHANNEL_ALMANAC, Type.ALMANAC) // bi-weekly history
            .build();

    Map<String, Type> HEAT_ALLOCATOR_MAPPING_69 = ImmutableMap.<String, Record.Type> builder()
            .putAll(TECHEM_METER_MAPPING) // inherit main HKV channel map
            .put(CHANNEL_ROOMTEMPERATURE, Type.ROOM_TEMPERATURE) // room
            .put(CHANNEL_RADIATORTEMPERATURE, Type.RADIATOR_TEMPERATURE) // radiator
            .build();

    // measurement readings
    Map<String, Type> SMOKE_DETECTOR_MAPPING = ImmutableMap.<String, Record.Type> builder()
            .put(CHANNEL_STATUS, Type.STATUS) // present date
            .put(CHANNEL_CURRENTDATE, Type.CURRENT_READING_DATE) // present date
            .put(CHANNEL_CURRENTDATE_STRING, Type.CURRENT_READING_DATE) // present date
            .put(CHANNEL_CURRENTDATE_NUMBER, Type.CURRENT_READING_DATE) // present date
            .put(CHANNEL_CURRENTDATE_SMOKE, Type.CURRENT_READING_DATE_SMOKE) // smoke detector 2nd date
            .put(CHANNEL_CURRENTDATE_SMOKE_STRING, Type.CURRENT_READING_DATE_SMOKE) // smoke detector 2nd date
            .put(CHANNEL_CURRENTDATE_SMOKE_NUMBER, Type.CURRENT_READING_DATE_SMOKE) // smoke detector 2nd date
            .build();

    // channel mapping for thing types
    Map<ThingTypeUID, Map<String, Type>> RECORD_MAP = ImmutableMap.<ThingTypeUID, Map<String, Type>> builder()
            .put(THING_TYPE_TECHEM_WARM_WATER_METER, TECHEM_METER_MAPPING) // warm
            .put(THING_TYPE_TECHEM_COLD_WATER_METER, TECHEM_METER_MAPPING) // cold

            .put(THING_TYPE_TECHEM_HEAT_METER, TECHEM_METER_MAPPING) // heat meter have same set of channels as heat
                                                                     // cost allocator
            .put(THING_TYPE_TECHEM_HKV45, TECHEM_METER_MAPPING) // basic HKV mapping
            .put(THING_TYPE_TECHEM_HKV61, TECHEM_METER_MAPPING) // basic HKV mapping
            .put(THING_TYPE_TECHEM_HKV64, TECHEM_METER_MAPPING) // again basic HKV mapping
            .put(THING_TYPE_TECHEM_HKV69, HEAT_ALLOCATOR_MAPPING_69) // here we have two temperature channels
            .put(THING_TYPE_TECHEM_HKV94, HEAT_ALLOCATOR_MAPPING_69) // try to decode 0x94 variant in same way as 0x69
            .put(THING_TYPE_TECHEM_SD76, SMOKE_DETECTOR_MAPPING) // v118 is smoke detector, experimental channels apply
            .build();
}
