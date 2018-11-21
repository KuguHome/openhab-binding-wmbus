/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device.techem;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.wmbus.device.techem.decoder.TechemFrameDecoder;
import org.openhab.binding.wmbus.device.techem.handler.TechemHKVHandler;
import org.openhab.binding.wmbus.device.techem.handler.TechemMeterHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TechemHandlerFactory} covers logic specific to TechemH devices.
 *
 * @author Łukasz Dywicki - Initial contribution
 */

@Component(service = { TechemHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class })
public class TechemHandlerFactory extends BaseThingHandlerFactory {

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(TechemHandlerFactory.class);

    private TechemFrameDecoder<TechemDevice> techemFrameDecoder;

    public TechemHandlerFactory() {
        logger.debug("Techem handler factory starting up.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return TechemBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(TechemBindingConstants.THING_TYPE_TECHEM_HKV)) {
            logger.warn("Detected legacy thing type, please migrate!", thing.getUID().getId());
            return new TechemHKVHandler(thing, techemFrameDecoder);
        }

        if (thingTypeUID.equals(TechemBindingConstants.THING_TYPE_TECHEM_HKV61)) {
            logger.debug("Creating handler for TechemDevice device {}", thing.getUID().getId());
            return new TechemMeterHandler<>(thing, TechemHeatCostAllocator.class, techemFrameDecoder);
        } else if (thingTypeUID.equals(TechemBindingConstants.THING_TYPE_TECHEM_HKV76)) {
            logger.debug("Creating handler for TechemDevice device {}", thing.getUID().getId());
            return new TechemMeterHandler<>(thing, TechemHeatCostAllocator.class, techemFrameDecoder);
        } else if (thingTypeUID.equals(TechemBindingConstants.THING_TYPE_TECHEM_HKV64)) {
            logger.debug("Creating handler for TechemDevice device {}", thing.getUID().getId());
            return new TechemMeterHandler<>(thing, TechemHeatCostAllocator.class, techemFrameDecoder);
        } else if (thingTypeUID.equals(TechemBindingConstants.THING_TYPE_TECHEM_HKV69)) {
            logger.debug("Creating handler for TechemDevice device {}", thing.getUID().getId());
            return new TechemMeterHandler<>(thing, TechemHeatCostAllocator.class, techemFrameDecoder);
        } else if (thingTypeUID.equals(TechemBindingConstants.THING_TYPE_TECHEM_WARM_WATER_METER)) {
            logger.debug("Creating handler for TechemDevice device {}", thing.getUID().getId());
            return new TechemMeterHandler<>(thing, TechemWaterMeter.class, techemFrameDecoder);
        } else if (thingTypeUID.equals(TechemBindingConstants.THING_TYPE_TECHEM_COLD_WATER_METER)) {
            logger.debug("Creating handler for TechemDevice device {}", thing.getUID().getId());
            return new TechemMeterHandler<>(thing, TechemWaterMeter.class, techemFrameDecoder);
        } else if (thingTypeUID.equals(TechemBindingConstants.THING_TYPE_TECHEM_HEAT_METER)) {
            logger.debug("Creating handler for TechemDevice device {}", thing.getUID().getId());
            return new TechemMeterHandler<>(thing, TechemWaterMeter.class, techemFrameDecoder);
        }

        logger.warn("Unsupported thing type {}. TechemHandlerFactory can not handle {}", thingTypeUID, thing.getUID());

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

    @Reference
    public void setTechemFrameDecoder(TechemFrameDecoder<TechemDevice> decoder) {
        this.techemFrameDecoder = decoder;
    }

    public void unsetTechemFrameDecoder(TechemFrameDecoder<TechemDevice> decoder) {
        this.techemFrameDecoder = null;
    }

}
