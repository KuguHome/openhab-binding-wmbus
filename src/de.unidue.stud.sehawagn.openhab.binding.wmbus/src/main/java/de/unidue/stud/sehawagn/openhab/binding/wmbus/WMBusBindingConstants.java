package de.unidue.stud.sehawagn.openhab.binding.wmbus;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

public class WMBusBindingConstants {

    public static final String BINDING_ID = "wmbus";
    public static final String THING_TYPE_NAME_BRIDGE = "wmbusbridge";
    public static final String THING_TYPE_NAME_TECHEM_HKV = "techem_hkv";

    // List all Thing Type UIDs, related to the WMBus Binding
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_BRIDGE);
    public final static ThingTypeUID THING_TYPE_TECHEM_HKV = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_TECHEM_HKV);

    // List all channels
    public static final String CHANNEL_ROOMTEMPERATURE = "room_temperature";
    public static final String CHANNEL_RADIATORTEMPERATURE = "radiator_temperature";
    public static final String CHANNEL_CURRENTREADING = "current_reading";
    public static final String CHANNEL_LASTREADING = "last_reading";
    public static final String CHANNEL_RECEPTION = "reception";
    public static final String CHANNEL_LASTDATE = "last_date";
    public static final String CHANNEL_CURRENTDATE = "current_date";
    public static final String CHANNEL_ALMANAC = "almanac";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BRIDGE, THING_TYPE_TECHEM_HKV);

    // Bridge config properties
    public static final String CONFKEY_INTERFACE_NAME = "serialDevice";
    // public static final String CONFKEY_POLLING_INTERVAL = "pollingInterval";
    public static final String CONFKEY_RADIO_MODE = "radioMode";

    // HKV config properties
    public static final String PROPERTY_HKV_ID = "hkvId";
    public static final String PROPERTY_WMBUS_MESSAGE = "wmBusMessage";

    // defaults
    public static final int DEFAULT_POLLING_INTERVAL = 10; // in seconds

}
