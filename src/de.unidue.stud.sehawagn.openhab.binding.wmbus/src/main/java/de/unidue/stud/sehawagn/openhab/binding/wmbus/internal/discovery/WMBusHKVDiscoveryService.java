package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.discovery;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusBridgeHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusMessageListener;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusDevice;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusHandlerFactory;

public class WMBusHKVDiscoveryService extends AbstractDiscoveryService implements WMBusMessageListener {

    private final Logger logger = LoggerFactory.getLogger(WMBusHKVDiscoveryService.class);

    private final static int SEARCH_TIME = 480; // wait some time, because the interval of all HCAs may be long

    // @formatter:off
    // add new devices here; these IDs here are generated in getThingTypeUID()
    private final static Map<String, String> TYPE_TO_WMBUS_ID_MAP = new ImmutableMap.Builder<String, String>()
            .put("68TCH105255", THING_TYPE_NAME_TECHEM_HKV)
            //TODO get IDs from log
            //.put("", THING_TYPE_NAME_KAMSTRUP_MULTICAL_302)
            //.put("", THING_TYPE_NAME_QUNDIS_QHEAT_5)
            .put("68QDS227", THING_TYPE_NAME_QUNDIS_QWATER_5_5)
            .put("68QDS528", THING_TYPE_NAME_QUNDIS_QCALORIC_5_5)
            .build();

    private WMBusBridgeHandler bridgeHandler;

    public WMBusHKVDiscoveryService(WMBusBridgeHandler bridgeHandler) {
        super(SEARCH_TIME);
        this.bridgeHandler = bridgeHandler;
    }

    public WMBusHKVDiscoveryService(Set<ThingTypeUID> supportedThingTypes, int timeout, boolean backgroundDiscoveryEnabledByDefault) throws IllegalArgumentException {
        super(supportedThingTypes, timeout, backgroundDiscoveryEnabledByDefault);
    }

    @Override
    // add new devices there
    public Set<ThingTypeUID> getSupportedThingTypes() {
        logger.trace("discovery: getSupportedThingTypes(): currently *implemented* are " + WMBusHandlerFactory.SUPPORTED_THING_TYPES_UIDS.toString());
        return WMBusHandlerFactory.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        // do nothing since there is no active scan possible at the moment, only receiving
        logger.debug(
                "discovery: startScan(): unimplemented - devices will be added upon reception of telegrams from them");
    }

    @Override
    public void onNewWMBusDevice(WMBusDevice wmBusDevice) {
        logger.debug("discovery: onnewWMBusDevice(): new device " + wmBusDevice.getOriginalMessage().getSecondaryAddress().toString());
        onWMBusMessageReceivedInternal(wmBusDevice);
    }

    private void onWMBusMessageReceivedInternal(WMBusDevice wmBusDevice) {
        logger.trace("discovery: msgreceivedInternal() begin");
        // try to find this device in the list of supported devices
        ThingUID thingUID = getThingUID(wmBusDevice);

        if (thingUID != null) {
            logger.trace("discorvey: msgreceivedInternal(): device is known");
            // device known -> create discovery result
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(PROPERTY_HKV_ID, wmBusDevice.getDeviceId().toString());

            // TODO label according to uid
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(wmBusDevice.getDeviceId().toString())
                    .withBridge(bridgeUID)
                    .withLabel("WMBus device ser.no. " + wmBusDevice.getDeviceId()).build();
            logger.trace("discorvey: msgreceivedInternal(): notifying OpenHAB of new thing");
            thingDiscovered(discoveryResult);
        } else {
            // device unknown -> log message
            logger.debug("discovered unsupported WMBus device with our type ID {} of WMBus type '{}' with secondary address {}",
                    getTypeID(wmBusDevice),
                    wmBusDevice.getOriginalMessage().getSecondaryAddress().getDeviceType(),
                    wmBusDevice.getOriginalMessage().getSecondaryAddress().toString()
                    );
        }
    }

    // checks if this device is of the supported kind -> if yes, will be discovered
    private ThingUID getThingUID(WMBusDevice wmBusDevice) {
        logger.trace("discover: getThingUID begin");
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = getThingTypeUID(wmBusDevice);

        if (thingTypeUID != null && getSupportedThingTypes().contains(thingTypeUID)) {
            logger.trace("discover: getThingUID have bridgeUID " + bridgeUID.toString());
            logger.trace("discover: getThingUID have thingTypeUID " + thingTypeUID.toString());
            return new ThingUID(thingTypeUID, bridgeUID, wmBusDevice.getDeviceId() + "");
        } else {
            logger.debug("discover: get ThingUID found no supported device");
            return null;
        }
    }

    private ThingTypeUID getThingTypeUID(WMBusDevice wmBusDevice) {
        String typeIdString = getTypeID(wmBusDevice);
        logger.debug("discovery: getThingTypeUID(): This device has typeID " + typeIdString
                + " -- supported device types are "
                + Arrays.toString(WMBusHKVDiscoveryService.TYPE_TO_WMBUS_ID_MAP.keySet().toArray()));
        String thingTypeId = TYPE_TO_WMBUS_ID_MAP.get(typeIdString);
        return thingTypeId != null ? new ThingTypeUID(BINDING_ID, thingTypeId) : null;
    }

    // this ID is needed for add new devices
    private String getTypeID(WMBusDevice wmBusDevice) {
        return wmBusDevice.getOriginalMessage().getControlField() + "" + wmBusDevice.getOriginalMessage().getSecondaryAddress().getManufacturerId()
                + "" + wmBusDevice.getOriginalMessage().getSecondaryAddress().getVersion() + ""
                + wmBusDevice.getOriginalMessage().getSecondaryAddress().getDeviceType().getId();
    }

    public void activate() {
        logger.debug("discovery: activate: registering as wmbusmessagelistener");
        bridgeHandler.registerWMBusMessageListener(this);
    }

    @Override
    public void onChangedWMBusDevice(WMBusDevice wmBusDevice) {
        // nothing to do
    }

}
