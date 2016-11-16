package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.discovery;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openmuc.jmbus.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusBridgeHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusMessageListener;

public class WMBusHKVDiscoveryService extends AbstractDiscoveryService implements WMBusMessageListener {

    private final Logger logger = LoggerFactory.getLogger(WMBusHKVDiscoveryService.class);

    private final static int SEARCH_TIME = 480; // wait some time, because the interval of all HCAs may be long

    // @formatter:off
    private final static Map<String, String> TYPE_TO_WMBUS_ID_MAP = new ImmutableMap.Builder<String, String>()
            .put("techem_hkv", "0100").build();
    // @formatter:on

    private WMBusBridgeHandler bridgeHandler;

    public WMBusHKVDiscoveryService(WMBusBridgeHandler bridgeHandler) {
        super(SEARCH_TIME);
        this.bridgeHandler = bridgeHandler;
    }

    public WMBusHKVDiscoveryService(Set<ThingTypeUID> supportedThingTypes, int timeout,
            boolean backgroundDiscoveryEnabledByDefault) throws IllegalArgumentException {
        super(supportedThingTypes, timeout, backgroundDiscoveryEnabledByDefault);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void startScan() {
        // do nothing since there is no active scan possible, only receival
    }

    @Override
    public void onWMBusMessageReceived(WMBusMessage wmBusDevice) {
        onWMBusMessageReceivedInternal(wmBusDevice);
    }

    private void onWMBusMessageReceivedInternal(WMBusMessage wmBusDevice) {
        ThingUID thingUID = getThingUID(wmBusDevice);
        if (thingUID != null) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(HKV_ID, wmBusDevice.getSecondaryAddress().getDeviceId());
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID).withLabel(wmBusDevice.getSecondaryAddress().getDeviceId() + "").build();
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported WMBus device of type '{}' with id {}",
                    wmBusDevice.getSecondaryAddress().getDeviceType(), wmBusDevice.getSecondaryAddress().getDeviceId());
        }
    }

    private ThingUID getThingUID(WMBusMessage wmBusDevice) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = getThingTypeUID(wmBusDevice);

        if (thingTypeUID != null && getSupportedThingTypes().contains(thingTypeUID)) {
            return new ThingUID(thingTypeUID, bridgeUID, wmBusDevice.getSecondaryAddress().getDeviceId() + "");
        } else {
            return null;
        }
    }

    private ThingTypeUID getThingTypeUID(WMBusMessage wmBusDevice) {
        String thingTypeId = TYPE_TO_WMBUS_ID_MAP.get(wmBusDevice.getSecondaryAddress().getManufacturerId()
                + wmBusDevice.getSecondaryAddress().getDeviceType().getId());
        System.out.println("thingTypeId" + thingTypeId);
        return thingTypeId != null ? new ThingTypeUID(BINDING_ID, thingTypeId) : null;
    }

}
