/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device.qloud;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.wmbus.device.qloud.handler.QloudThingHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QloudHandlerFactory} covers logic specific to Fastforward/Q-loud energy cam devices.
 *
 * @author Łukasz Dywicki - Initial contribution
 */
@Component(service = { QloudHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class })
public class QloudHandlerFactory extends BaseThingHandlerFactory {

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(QloudHandlerFactory.class);

    public QloudHandlerFactory() {
        logger.debug("Techem handler factory starting up.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return QloudBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(QloudBindingConstants.THING_TYPE_ENERGYCAM_OIL)
                || thingTypeUID.equals(QloudBindingConstants.THING_TYPE_ENERGYCAM_ELECTRICITY)
                || thingTypeUID.equals(QloudBindingConstants.THING_TYPE_ENERGYCAM_GAS)
                || thingTypeUID.equals(QloudBindingConstants.THING_TYPE_ENERGYCAM_WATER)) {
            logger.debug("Creating handler for q-loud meter {}", thing.getUID().getId());
            return new QloudThingHandler(thing);
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
