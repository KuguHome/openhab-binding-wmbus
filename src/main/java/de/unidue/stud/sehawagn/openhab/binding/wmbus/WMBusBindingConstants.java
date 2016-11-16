package de.unidue.stud.sehawagn.openhab.binding.wmbus;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

public class WMBusBindingConstants {

    public static final String BINDING_ID = "wmbus";

    // List all Thing Type UIDs, related to the WMBus Binding
    public final static ThingTypeUID THING_TYPE_WMBUS_BRIDGE = new ThingTypeUID(BINDING_ID, "wmbusbridge");
    public final static ThingTypeUID THING_TYPE_WMBUS_TECHEM_HKV = new ThingTypeUID(BINDING_ID, "techem_hkv");

    // List all channels
    public static final String CHANNEL_ROOMTEMPERATURE = "room_temperature";
    public static final String CHANNEL_RADIATORTEMPERATURE = "radiator_temperature";
    public static final String CHANNEL_COSTCOUNTER = "costcounter";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_WMBUS_BRIDGE);

    // Bridge config properties
    public static final String CONFKEY_INTERFACE_NAME = "serialDevice";
    // public static final String CONFKEY_POLLING_INTERVAL = "pollingInterval";

    // HKV config properties
    public static final String HKV_ID = "hkvId";

    // defaults
    public static final int DEFAULT_POLLING_INTERVAL = 10; // in seconds

}
