/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device.eielectronics;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.io.transport.mbus.wireless.FilteredKeyStorage;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EieHandlerFactory} covers logic specific to Eie devices.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */

@Component(service = { EieHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class })
public class EieHandlerFactory extends BaseThingHandlerFactory {

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(EieHandlerFactory.class);
    private KeyStorage keyStorage;
    private UnitRegistry unitRegistry;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return EieBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(EieBindingConstants.THING_TYPE_EIE_SMOKE_DETECTOR)) {
            logger.debug("Creating handler for Eie device {}", thing.getUID().getId());
            return new EieSmokeDetectorHandler(thing, new FilteredKeyStorage(keyStorage, thing), unitRegistry);
        }

        return null;
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

}
