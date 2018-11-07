/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openmuc.jmbus.DeviceType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * The {@link WMBusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hanno - Felix Wagner, Ernst Rohlicek, Roman Malyugin - Initial contribution
 * @author Łűkasz Dywicki - meter thing type and surroundings
 */

public class WMBusBindingConstants {

    public static final String BINDING_ID = "wmbus";
    public static final String THING_TYPE_NAME_BRIDGE = "wmbusbridge";
    public static final String THING_TYPE_NAME_VIRTUAL_BRIDGE = "wmbusvirtualbridge";
    public static final String THING_TYPE_NAME_METER = "meter";

    // add new devices here - string must not contain "." or you get InitializerError on WMBusHandlerFactory even before
    // constructor
    public static final String THING_TYPE_NAME_TECHEM_HKV = "techem_hkv"; // heat cost allocator (Heizkostenverteiler)
    public static final String THING_TYPE_NAME_KAMSTRUP_MULTICAL_302 = "kamstrup_multical_302"; // (water) heat meter
                                                                                                // (Wärmemengenzähler)
                                                                                                // with water (flow)
                                                                                                // meter
    public static final String THING_TYPE_NAME_QUNDIS_QHEAT_5 = "qundis_qheat_5"; // (water) heat meter
                                                                                  // (Wärmemengenzähler)
    public static final String THING_TYPE_NAME_QUNDIS_QWATER_5_5 = "qundis_qwater_5_5"; // water (flow) meter
                                                                                        // (Wasserzähler)
    public static final String THING_TYPE_NAME_QUNDIS_QCALORIC_5_5 = "qundis_qcaloric_5_5"; // heat cost allocator
                                                                                            // (Heizkostenverteiler)

    public static final String THING_TYPE_NAME_ENGELMANN_SENSOSTAR = "engelmann_heat_meter_v0";
    public static final String THING_TYPE_NAME_ADEUNIS_GAS_METER_3 = "adeunis_rf_gas_meter_v3";

    // List all Thing Type UIDs, related to the WMBus Binding
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_BRIDGE);
    public final static ThingTypeUID THING_TYPE_VIRTUAL_BRIDGE = new ThingTypeUID(BINDING_ID,
            THING_TYPE_NAME_VIRTUAL_BRIDGE);

    public final static ThingTypeUID THING_TYPE_METER = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_METER);

    // add new devices here
    public final static ThingTypeUID THING_TYPE_TECHEM_HKV = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_TECHEM_HKV);
    public final static ThingTypeUID THING_TYPE_KAMSTRUP_MULTICAL_302 = new ThingTypeUID(BINDING_ID,
            THING_TYPE_NAME_KAMSTRUP_MULTICAL_302);
    public final static ThingTypeUID THING_TYPE_QUNDIS_QHEAT_5 = new ThingTypeUID(BINDING_ID,
            THING_TYPE_NAME_QUNDIS_QHEAT_5);
    public final static ThingTypeUID THING_TYPE_QUNDIS_QWATER_5_5 = new ThingTypeUID(BINDING_ID,
            THING_TYPE_NAME_QUNDIS_QWATER_5_5);
    public final static ThingTypeUID THING_TYPE_QUNDIS_QCALORIC_5_5 = new ThingTypeUID(BINDING_ID,
            THING_TYPE_NAME_QUNDIS_QCALORIC_5_5);
    public static final ThingTypeUID THING_TYPE_ENGELMANN_SENSOSTAR = new ThingTypeUID(BINDING_ID,
            THING_TYPE_NAME_ENGELMANN_SENSOSTAR);
    public static final ThingTypeUID THING_TYPE_ADEUNIS_GAS_METER_3 = new ThingTypeUID(BINDING_ID,
            THING_TYPE_NAME_ADEUNIS_GAS_METER_3);

    // List all channels
    // general channels
    public static final String CHANNEL_RECEPTION = "reception";
    public static final String CHANNEL_ALMANAC = "almanac";
    public static final String CHANNEL_ERRORDATE = "error_date";
    public static final String CHANNEL_ERRORFLAGS = "error_flags";

    // temperatures
    public static final String CHANNEL_ROOMTEMPERATURE = "room_temperature";
    public static final String CHANNEL_RADIATORTEMPERATURE = "radiator_temperature";

    // measurement readings
    public static final String CHANNEL_CURRENTREADING = "current_reading";
    public static final String CHANNEL_CURRENTPOWER = "current_power_w";
    public static final String CHANNEL_CURRENTENERGYTOTAL = "current_energy_total_kwh";
    public static final String CHANNEL_CURRENTVOLUMEFLOW = "current_volume_flow_m3h";
    public static final String CHANNEL_CURRENTVOLUMETOTAL = "current_volume_total_m3";
    public static final String CHANNEL_CURRENTDATE = "current_date";
    public static final String CHANNEL_RETURNTEMPERATURE = "return_temperature";
    public static final String CHANNEL_TEMPERATUREDIFFERENCE = "temperature_difference";

    public static final String CHANNEL_PREVIOUSREADING = "previous_reading";
    public static final String CHANNEL_PREVIOUSENERGYTOTAL = "previous_energy_total_kwh";
    public static final String CHANNEL_PREVIOUSDATE = "previous_date";

    public static final String CHANNEL_LASTREADING = "last_reading";
    public static final String CHANNEL_LASTDATE = "last_date";

    // add new devices here
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BRIDGE,
            THING_TYPE_TECHEM_HKV, THING_TYPE_KAMSTRUP_MULTICAL_302, THING_TYPE_QUNDIS_QHEAT_5,
            THING_TYPE_QUNDIS_QWATER_5_5, THING_TYPE_QUNDIS_QCALORIC_5_5, THING_TYPE_VIRTUAL_BRIDGE);

    // Bridge config properties
    public static final String CONFKEY_STICK_MODEL = "stickModel";
    public static final String CONFKEY_INTERFACE_NAME = "serialDevice";
    public static final String CONFKEY_RADIO_MODE = "radioMode";
    public static final String CONFKEY_ENCRYPTION_KEYS = "encryptionKeys";
    public static final String CONFKEY_DEVICEID_FILTER = "deviceIDFilter";

    public static final String CONFKEY_VIRTUAL_CF = "control_field";
    public static final String CONFKEY_VIRTUAL_BYTES = "bytes";
    public static final String CONFKEY_VIRTUAL_ADDRESS = "address";
    public static final String CONFKEY_VIRTUAL_RSSI = "rssi";

    // device config properties
    public static final String PROPERTY_DEVICE_ID = "deviceId";
    public static final String PROPERTY_DEVICE_ADDRESS = "address";
    public static final String PROPERTY_WMBUS_MESSAGE = "wmBusMessage";

    public static final Map<String, String> WMBUS_TYPE_MAP = new ImmutableMap.Builder<String, String>()
            .put("68TCH97255", THING_TYPE_NAME_TECHEM_HKV) // unsure,
            // whether they work
            .put("68TCH105255", THING_TYPE_NAME_TECHEM_HKV)
            // another techem
            .put("68TCH116255", THING_TYPE_NAME_TECHEM_HKV) // unsure,
            // whether they work
            .put("68TCH118255", THING_TYPE_NAME_TECHEM_HKV) // find out, if they work
            .put("68KAM484", THING_TYPE_NAME_KAMSTRUP_MULTICAL_302).put("68LSE264", THING_TYPE_NAME_QUNDIS_QHEAT_5)
            .put("68QDS227", THING_TYPE_NAME_QUNDIS_QWATER_5_5).put("68QDS528", THING_TYPE_NAME_QUNDIS_QCALORIC_5_5)
            .put("68EFE04", THING_TYPE_NAME_ENGELMANN_SENSOSTAR).put("68ARF33", THING_TYPE_NAME_ADEUNIS_GAS_METER_3)
            .build();

    /**
     * Generic device types which are supported by binding.
     */
    public static final Set<DeviceType> SUPPORTED_DEVICE_TYPES = ImmutableSet.of(DeviceType.OIL_METER,
            DeviceType.ELECTRICITY_METER, DeviceType.GAS_METER, DeviceType.HEAT_METER, DeviceType.STEAM_METER,
            DeviceType.WARM_WATER_METER, DeviceType.WATER_METER, DeviceType.HEAT_COST_ALLOCATOR,
            DeviceType.COMPRESSED_AIR, DeviceType.COOLING_METER_OUTLET, DeviceType.COOLING_METER_INLET,
            DeviceType.HEAT_METER_INLET, DeviceType.HEAT_COOLING_METER, DeviceType.CALORIFIC_VALUE,
            DeviceType.HOT_WATER_METER, DeviceType.COLD_WATER_METER, DeviceType.DUAL_REGISTER_WATER_METER,
            DeviceType.PRESSURE_METER, DeviceType.SMOKE_DETECTOR, DeviceType.ROOM_SENSOR_TEMP_HUM,
            DeviceType.GAS_DETECTOR, DeviceType.BREAKER_ELEC, DeviceType.VALVE_GAS_OR_WATER,
            DeviceType.WASTE_WATER_METER);

}
