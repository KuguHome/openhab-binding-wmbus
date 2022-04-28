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
package org.openhab.binding.wmbus;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openmuc.jmbus.DeviceType;

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
    public static final String THING_TYPE_NAME_ENCRYPTED_METER = "encrypted_meter";

    /**
     * Time to live - by default 24 hours after which discovery result is discarded.
     */
    public static final Long DEFAULT_TIME_TO_LIVE = TimeUnit.HOURS.toSeconds(24);

    // List all Thing Type UIDs, related to the WMBus Binding
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_BRIDGE);
    public final static ThingTypeUID THING_TYPE_VIRTUAL_BRIDGE = new ThingTypeUID(BINDING_ID,
            THING_TYPE_NAME_VIRTUAL_BRIDGE);

    public final static ThingTypeUID THING_TYPE_METER = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_METER);
    public final static ThingTypeUID THING_TYPE_ENCRYPTED_METER = new ThingTypeUID(BINDING_ID,
            THING_TYPE_NAME_ENCRYPTED_METER);

    public static final String CHANNEL_LAST_FRAME = "last_frame";
    public static final String CHANNEL_ERRORDATE = "error_date";
    public static final String CHANNEL_ERRORFLAGS = "error_flags";

    public static final String CHANNEL_CURRENTPOWER = "current_power_w";
    public static final String CHANNEL_CURRENTENERGYTOTAL = "current_energy_total_kwh";
    public static final String CHANNEL_CURRENTVOLUMEFLOW = "current_volume_flow_m3h";
    public static final String CHANNEL_CURRENTVOLUMETOTAL = "current_volume_total_m3";
    public static final String CHANNEL_RETURNTEMPERATURE = "return_temperature";
    public static final String CHANNEL_TEMPERATUREDIFFERENCE = "temperature_difference";

    public static final String CHANNEL_PREVIOUSREADING = "previous_reading";
    public static final String CHANNEL_PREVIOUSENERGYTOTAL = "previous_energy_total_kwh";
    public static final String CHANNEL_PREVIOUSDATE = "previous_date";

    public final static ChannelTypeUID CHANNEL_LAST_FRAME_TYPE = new ChannelTypeUID(BINDING_ID, CHANNEL_LAST_FRAME);

    // add new devices here
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BRIDGE,
            THING_TYPE_VIRTUAL_BRIDGE, THING_TYPE_METER, THING_TYPE_ENCRYPTED_METER);

    // Bridge config properties
    public static final String CONFKEY_STICK_MODEL = "stickModel";
    public static final String CONFKEY_INTERFACE_NAME = "serialDevice";
    public static final String CONFKEY_RADIO_MODE = "radioMode";
    public static final String CONFKEY_DATEFIELD_MODE = "dateFieldMode";
    public static final String CONFKEY_ENCRYPTION_KEYS = "encryptionKeys";
    public static final String CONFKEY_DEVICEID_FILTER = "deviceIDFilter";

    // device config properties
    public static final String PROPERTY_DEVICE_ADDRESS = "deviceAddress";
    public static final String PROPERTY_DEVICE_FREQUENCY_OF_UPDATES = "frequencyOfUpdates";
    public static final String PROPERTY_DEVICE_ENCRYPTION_KEY = "encryptionKey";
    // device property which says if we expected secure communication
    public static final String PROPERTY_DEVICE_ENCRYPTED = "encrypted";

    public static final String PROPERTY_WMBUS_MESSAGE = "wmBusMessage";

    // Manufacturer options
    public static final String MANUFACTURER_AMBER = "amber";
    public static final String MANUFACTURER_RADIO_CRAFTS = "rc";
    public static final String MANUFACTURER_IMST = "imst";
    public static final String MANUFACTURER_CUL = "cul";

    /**
     * Default frequency of reports. This is at the same time value after which device is considered to be offline.
     * Value in minutes.
     */
    public static final Long DEFAULT_DEVICE_FREQUENCY_OF_UPDATES = 60l;

    /**
     * A default encryption key.
     */
    public static final byte[] DEFAULT_DEVICE_ENCRYPTION_KEY = new byte[0];

    public static final Function<DeviceType, String> DEVICE_TYPE_TRANSFORMATION = deviceType -> deviceType.name()
            .toLowerCase().replace("_", " ");

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
            DeviceType.WASTE_WATER_METER, DeviceType.RADIO_CONVERTER_SYSTEM_SIDE,
            DeviceType.RADIO_CONVERTER_METER_SIDE);

    public static final String CONFKEY_BINDING_TIME_TO_LIVE = "timeToLive";
    public static final String CONFKEY_BINDING_INCLUDE_BRIDGE_UID = "includeBridgeUID";
}
