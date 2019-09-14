/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.internal;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.device.UnknownMeter.UnknownWMBusDeviceHandler;
import org.openhab.binding.wmbus.device.generic.DynamicWMBusThingHandler;
import org.openhab.binding.wmbus.discovery.CompositeMessageListener;
import org.openhab.binding.wmbus.handler.WMBusBridgeHandler;
import org.openhab.binding.wmbus.handler.WMBusMessageListener;
import org.openhab.io.transport.mbus.wireless.FilteredKeyStorage;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WMBusHandlerFactory} class defines WMBusHandlerFactory. This class is the main entry point of the binding.
 *
 * @author Hanno - Felix Wagner - Roman Malyugin - Initial contribution
 */

@Component(service = { WMBusHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class })
public class WMBusHandlerFactory extends BaseThingHandlerFactory {

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(WMBusHandlerFactory.class);

    private final CompositeMessageListener messageListener = new CompositeMessageListener();

    private KeyStorage keyStorage;
    private UnitRegistry unitRegistry;
    private WMBusChannelTypeProvider channelTypeProvider;

    public WMBusHandlerFactory() {
        logger.debug("wmbus handler factory is starting up.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return WMBusBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_BRIDGE)) {
            // create handler for WMBus bridge
            logger.debug("Creating (handler for) WMBus bridge.");
            if (thing instanceof Bridge) {
                WMBusBridgeHandler handler = new WMBusBridgeHandler((Bridge) thing, keyStorage);
                handler.registerWMBusMessageListener(messageListener);
                return handler;
            } else {
                logger.warn("An attempt to create wmbus bridge from regular thing {}. Skipping request.", thing);
                return null;
            }
        }

        // add new devices here
        if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_METER)
                || thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_ENCRYPTED_METER)) {
            logger.debug("Creating standard wmbus handler.");
            return new DynamicWMBusThingHandler<>(thing, new FilteredKeyStorage(keyStorage, thing), unitRegistry,
                    channelTypeProvider);
        } else {
            logger.debug("Creating (handler for) Unknown device.");
            return new UnknownWMBusDeviceHandler(thing, new FilteredKeyStorage(keyStorage, thing));
        }
    }

    @Override
    @Activate
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Override
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void registerWMBusMessageListener(WMBusMessageListener wmBusMessageListener) {
        messageListener.addMessageListener(wmBusMessageListener);
    }

    public void unregisterWMBusMessageListener(WMBusMessageListener wmBusMessageListener) {
        messageListener.removeMessageListener(wmBusMessageListener);
    }

    @Reference
    public void setKeyStorage(KeyStorage keyStorage) {
        this.keyStorage = keyStorage;
    }

    public void unsetKeyStorage(KeyStorage keyStorage) {
        this.keyStorage = null;
    }

    @Reference
    protected void setUnitRegistry(UnitRegistry unitRegistry) {
        this.unitRegistry = unitRegistry;
    }

    protected void unsetUnitRegistry(UnitRegistry unitRegistry) {
        this.unitRegistry = null;
    }

    @Reference
    protected void setChannelTypeProvider(WMBusChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = channelTypeProvider;
    }

    protected void unsetChannelTypeProvider(WMBusChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = null;
    }

}
