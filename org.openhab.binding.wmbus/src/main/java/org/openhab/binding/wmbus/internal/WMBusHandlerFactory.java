/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.internal;

import static org.openhab.binding.wmbus.WMBusBindingConstants.*;

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
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.device.ADEUNISGasMeter;
import org.openhab.binding.wmbus.device.EngelmannHeatMeter;
import org.openhab.binding.wmbus.device.UnknownMeter;
import org.openhab.binding.wmbus.handler.KamstrupMultiCal302Handler;
import org.openhab.binding.wmbus.handler.QundisQCaloricHandler;
import org.openhab.binding.wmbus.handler.QundisQHeatHandler;
import org.openhab.binding.wmbus.handler.QundisQWaterHandler;
import org.openhab.binding.wmbus.handler.TechemHKVHandler;
import org.openhab.binding.wmbus.handler.WMBusBridgeHandler;
import org.openhab.binding.wmbus.handler.WMBusVirtualBridgeHandler;
import org.openhab.binding.wmbus.internal.discovery.WMBusDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * The {@link WMBusHandlerFactory} class defines WMBusHandlerFactory. This class is the main entry point of the binding.
 *
 * @author Hanno - Felix Wagner - Roman Malyugin - Initial contribution
 */

@Component(service = { WMBusHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class })
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
        }
        if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_VIRTUAL_BRIDGE)) {
            logger.debug("Creating (handler for) WMBus virtual bridge.");
            if (thing instanceof Bridge) {
                WMBusBridgeHandler handler = new WMBusVirtualBridgeHandler((Bridge) thing);
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
        } else if (thingTypeUID.equals(adeunisGasMeter.getThingType())) {
            logger.debug("Creating (handler for) ADEUNIS_RF Gas Meter (v.3) device.");
            return adeunisGasMeter.new ADEUNISGasMeterHandler(thing);
        } else if (thingTypeUID.equals(engelmannHeatMeter.getThingType())) {
            logger.debug("Creating (handler for) Engelmann Heat Meter device.");
            return engelmannHeatMeter.new EngelmannHeatMeterHandler(thing);
        } else {
            logger.debug("Creating (handler for) Unknown device.");
            return unknownMeter.new UnknownWMBusDeviceHandler(thing);
        }
    }

    private synchronized void registerDiscoveryService(WMBusBridgeHandler bridgeHandler) {
        logger.debug("Registering discovery service.");
        Map<String, String> typeToWMBUSIdMap = new ImmutableMap.Builder<String, String>()
                .put("68TCH97255", THING_TYPE_NAME_TECHEM_HKV) // unsure,
                // whether
                // they
                // work
                .put("68TCH105255", THING_TYPE_NAME_TECHEM_HKV).put("68TCH116255", THING_TYPE_NAME_TECHEM_HKV) // unsure,
                // whether
                // they
                // work
                .put("68TCH118255", THING_TYPE_NAME_TECHEM_HKV) // find out, if they work
                .put("68KAM484", THING_TYPE_NAME_KAMSTRUP_MULTICAL_302).put("68LSE264", THING_TYPE_NAME_QUNDIS_QHEAT_5)
                .put("68QDS227", THING_TYPE_NAME_QUNDIS_QWATER_5_5).put("68QDS528", THING_TYPE_NAME_QUNDIS_QCALORIC_5_5)
                .put(engelmannHeatMeter.getThingTypeId(), engelmannHeatMeter.getThingTypeName())
                .put(adeunisGasMeter.getThingTypeId(), adeunisGasMeter.getThingTypeName()).build();

        WMBusDiscoveryService discoveryService = new WMBusDiscoveryService(bridgeHandler, typeToWMBUSIdMap);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    @Activate
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        SUPPORTED_THING_TYPES_UIDS = ImmutableSet
                .of(WMBusBridgeHandler.SUPPORTED_THING_TYPES, TechemHKVHandler.SUPPORTED_THING_TYPES,
                        QundisQCaloricHandler.SUPPORTED_THING_TYPES, QundisQWaterHandler.SUPPORTED_THING_TYPES,
                        QundisQHeatHandler.SUPPORTED_THING_TYPES, KamstrupMultiCal302Handler.SUPPORTED_THING_TYPES,
                        WMBusVirtualBridgeHandler.SUPPORTED_THING_TYPES, engelmannHeatMeter.getSupportedThingTypes(),
                        adeunisGasMeter.getSupportedThingTypes()/*
                                                                 * ,
                                                                 * unknownMeter.getSupportedThingTypes()
                                                                 */)
                .stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    volatile protected ADEUNISGasMeter adeunisGasMeter;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    volatile protected EngelmannHeatMeter engelmannHeatMeter;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    volatile protected UnknownMeter unknownMeter;
}
