package de.unidue.stud.sehawagn.openhab.binding.wmbus;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

public class WMBusBindingConstants {

    public static final String BINDING_ID = "wmbus";
    public static final String THING_TYPE_NAME_BRIDGE = "wmbusbridge";
    // add new devices here - string must not contain "." or you get InitializerError on WMBusHandlerFactory even before
    // constructor
    public static final String THING_TYPE_NAME_TECHEM_HKV = "techem_hkv"; // heat cost allocator (Heizkostenverteiler)
    public static final String THING_TYPE_NAME_KAMSTRUP_MULTICAL_302 = "kamstrup_multical_302"; // (water) heat meter (Wärmemengenzähler) with water (flow) meter
    public static final String THING_TYPE_NAME_QUNDIS_QHEAT_5 = "qundis_qheat_5"; // (water) heat meter (Wärmemengenzähler)
    public static final String THING_TYPE_NAME_QUNDIS_QWATER_5_5 = "qundis_qwater_5_5"; // water (flow) meter (Wasserzähler)
    public static final String THING_TYPE_NAME_QUNDIS_QCALORIC_5_5 = "qundis_qcaloric_5_5"; // heat cost allocator (Heizkostenverteiler)

    // List all Thing Type UIDs, related to the WMBus Binding
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_BRIDGE);
    // add new devices here
    public final static ThingTypeUID THING_TYPE_TECHEM_HKV = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_TECHEM_HKV);
    public final static ThingTypeUID THING_TYPE_KAMSTRUP_MULTICAL_302 = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_KAMSTRUP_MULTICAL_302);
    public final static ThingTypeUID THING_TYPE_QUNDIS_QHEAT_5 = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_QUNDIS_QHEAT_5);
    public final static ThingTypeUID THING_TYPE_QUNDIS_QWATER_5_5 = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_QUNDIS_QWATER_5_5);
    public final static ThingTypeUID THING_TYPE_QUNDIS_QCALORIC_5_5 = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_QUNDIS_QCALORIC_5_5);

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
    public static final String CHANNEL_CURRENTENERGYTOTAL = "current_energy_total_wh";
    public static final String CHANNEL_CURRENTVOLUMEFLOW = "current_volume_flow_m3h";
    public static final String CHANNEL_CURRENTVOLUMETOTAL = "current_volume_total_m3";
    public static final String CHANNEL_CURRENTDATE = "current_date";

    public static final String CHANNEL_PREVIOUSREADING = "previous_reading";
    public static final String CHANNEL_PREVIOUSENERGYTOTAL = "previous_energy_total_wh";
    public static final String CHANNEL_PREVIOUSDATE = "previous_date";

    public static final String CHANNEL_LASTREADING = "last_reading";
    public static final String CHANNEL_LASTDATE = "last_date";

    // add new devices here
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BRIDGE, THING_TYPE_TECHEM_HKV, THING_TYPE_KAMSTRUP_MULTICAL_302, THING_TYPE_QUNDIS_QHEAT_5, THING_TYPE_QUNDIS_QWATER_5_5, THING_TYPE_QUNDIS_QCALORIC_5_5);

    // Bridge config properties
    public static final String CONFKEY_STICK_MODEL = "stickModel";
    public static final String CONFKEY_INTERFACE_NAME = "serialDevice";
    public static final String CONFKEY_RADIO_MODE = "radioMode";
    public static final String CONFKEY_ENCRYPTION_KEYS = "encryptionKeys";
    public static final String CONFKEY_DEVICEID_FILTER = "deviceIDFilter";

    // public static final String CONFKEY_POLLING_INTERVAL = "pollingInterval";

    // HKV config properties
    public static final String PROPERTY_HKV_ID = "hkvId"; // TODO change to WmbusDevice_ID
    public static final String PROPERTY_WMBUS_MESSAGE = "wmBusMessage";

    // defaults
    public static final int DEFAULT_POLLING_INTERVAL = 10; // in seconds

}
