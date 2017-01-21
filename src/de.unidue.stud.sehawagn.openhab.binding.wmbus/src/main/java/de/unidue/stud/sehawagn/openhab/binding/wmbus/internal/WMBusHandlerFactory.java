package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.framework.ServiceRegistration;

import com.google.common.collect.Sets;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusBridgeHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusTechemHKVHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.discovery.WMBusHKVDiscoveryService;

public class WMBusHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .union(WMBusBridgeHandler.SUPPORTED_THING_TYPES, WMBusTechemHKVHandler.SUPPORTED_THING_TYPES);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_BRIDGE)) {
            if (thing instanceof Bridge) {
                WMBusBridgeHandler handler = new WMBusBridgeHandler((Bridge) thing);
                registerDiscoveryService(handler);
                return handler;
            } else {
                return null;
            }
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_TECHEM_HKV)) {
            return new WMBusTechemHKVHandler(thing);
        } else {
            return null;
        }
    }

    private synchronized void registerDiscoveryService(WMBusBridgeHandler bridgeHandler) {
        WMBusHKVDiscoveryService discoveryService = new WMBusHKVDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

}
