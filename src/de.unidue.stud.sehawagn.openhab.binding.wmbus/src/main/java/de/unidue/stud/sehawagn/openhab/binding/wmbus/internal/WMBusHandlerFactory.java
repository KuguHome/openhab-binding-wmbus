package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.ADEUNISGasMeterHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.KamstrupMultiCal302Handler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.QundisQCaloricHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.QundisQHeatHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.QundisQWaterHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.TechemHKVHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusBridgeHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.discovery.WMBusDiscoveryService;

/*
 * This class is the main entry point of the binding.
 */
@Component(service = { WMBusHandlerFactory.class, BaseThingHandlerFactory.class })
public class WMBusHandlerFactory extends BaseThingHandlerFactory {

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    public static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS;

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(WMBusHandlerFactory.class);

    public WMBusHandlerFactory() {
        logger.debug("wmbus binding starting up.");
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
            logger.debug("Creating (handler for) TechemHKV device.");
            return new TechemHKVHandler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_QUNDIS_QCALORIC_5_5)) {
            logger.debug("Creating (handler for) Qundis Qcaloric 5,5 device.");
            return new QundisQCaloricHandler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_QUNDIS_QWATER_5_5)) {
            logger.debug("Creating (handler for) Qundis Qwater 5,5 device.");
            return new QundisQWaterHandler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_QUNDIS_QHEAT_5)) {
            logger.debug("Creating (handler for) Qundis Qheat 5 device.");
            return new QundisQHeatHandler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_KAMSTRUP_MULTICAL_302)) {
            logger.debug("Creating (handler for) Kamstrup MultiCal 302 device.");
            return new KamstrupMultiCal302Handler(thing);
        } else if (thingTypeUID.equals(ADEUNISGasMeterHandler.THING_TYPE_ADEUNIS_GAS_METER_3)) {
            logger.debug("Creating (handler for) ADEUNIS_RF Gas Meter (v.3) device.");
            return new ADEUNISGasMeterHandler(thing);
        } else {
            return null;
        }
    }

    private synchronized void registerDiscoveryService(WMBusBridgeHandler bridgeHandler) {
        logger.debug("Registering discovery service.");
        WMBusDiscoveryService discoveryService = new WMBusDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Activate
    protected void activate(Map<String, String> properties) {
        SUPPORTED_THING_TYPES_UIDS = ImmutableSet
                .of(WMBusBridgeHandler.SUPPORTED_THING_TYPES, TechemHKVHandler.SUPPORTED_THING_TYPES,
                        QundisQCaloricHandler.SUPPORTED_THING_TYPES, QundisQWaterHandler.SUPPORTED_THING_TYPES,
                        QundisQHeatHandler.SUPPORTED_THING_TYPES, KamstrupMultiCal302Handler.SUPPORTED_THING_TYPES,
                        adeunisGasMeterHandler.getSupportedThingTypes())
                .stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Deactivate
    protected void deactivate() {
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    volatile protected ADEUNISGasMeterHandler adeunisGasMeterHandler;
}
