/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device.techem;

import static org.openhab.binding.wmbus.WMBusBindingConstants.THING_TYPE_TECHEM_HKV;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.handler.TechemHKVHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link TechemHandlerFactory} covers logic specific to TechemH devices.
 *
 * @author Łukasz Dywicki - Initial contribution
 */

@Component(service = { TechemHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class })
public class TechemHandlerFactory extends BaseThingHandlerFactory {

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(TechemHandlerFactory.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_TECHEM_HKV);

    public TechemHandlerFactory() {
        logger.debug("Techem handler factory starting up.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_TECHEM_HKV)) {
            logger.debug("Creating handler for TechemHKV device {}", thing.getUID().getId());
            return new TechemHKVHandler(thing);
        }

        logger.warn("Unsupported thing type {}. TechemHandlerFactory can not handle {}", thingTypeUID, thing);

        return null;
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

}
