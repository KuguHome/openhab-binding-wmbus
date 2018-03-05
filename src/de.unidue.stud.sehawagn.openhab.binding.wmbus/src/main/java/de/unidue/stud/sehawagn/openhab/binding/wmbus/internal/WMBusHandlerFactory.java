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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusBridgeHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusQundisQCaloricHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusQundisQWaterHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusTechemHKVHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.discovery.WMBusHKVDiscoveryService;

/*
 * This class is the main entry point of the binding.
 */
public class WMBusHandlerFactory extends BaseThingHandlerFactory {

    // add new devices here
    //TODO make this nicer instead of cascading
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(WMBusBridgeHandler.SUPPORTED_THING_TYPES, Sets.union(WMBusTechemHKVHandler.SUPPORTED_THING_TYPES, Sets.union(WMBusQundisQCaloricHandler.SUPPORTED_THING_TYPES, WMBusQundisQWaterHandler.SUPPORTED_THING_TYPES)));

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(WMBusHandlerFactory.class);

    public WMBusHandlerFactory() {
        logger.debug("WMBusHandlerFactory: wmbus binding starting up.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_BRIDGE)) {
            // create handler for WMBus bridge
            logger.debug("Creating (handler for) WMBus bridge.");
            if (thing instanceof Bridge) {
                WMBusBridgeHandler handler = new WMBusBridgeHandler((Bridge) thing);
                registerDiscoveryService(handler);
                return handler;
            } else {
                return null;
            }
            // add new devices here
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_TECHEM_HKV)) {
            // create handler for Techem HKV device
            logger.debug("Creating (handler for) TechemHKV device.");
            return new WMBusTechemHKVHandler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_QUNDIS_QCALORIC_5_5)) {
            // create handler for Qundis Qcaloric 5,5 device
            logger.debug("Creating (handler for) Qundis Qcaloric 5,5 device.");
            return new WMBusQundisQCaloricHandler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_QUNDIS_QWATER_5_5)) {
            // create handler for Qundis Qwater 5,5 device
            logger.debug("Creating (handler for) Qundis Qwater 5,5 device.");
            return new WMBusQundisQWaterHandler(thing);
        } else {
            return null;
        }
    }

    private synchronized void registerDiscoveryService(WMBusBridgeHandler bridgeHandler) {
        logger.debug("Registering discovery service.");
        WMBusHKVDiscoveryService discoveryService = new WMBusHKVDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

}
