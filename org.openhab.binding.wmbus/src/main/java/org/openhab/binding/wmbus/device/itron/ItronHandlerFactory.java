/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.wmbus.device.itron;

import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.io.transport.mbus.wireless.FilteredKeyStorage;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ItronHandlerFactory} covers logic specific to Itron devices.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */

@Component(service = { ItronHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class })
public class ItronHandlerFactory extends BaseThingHandlerFactory {

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(ItronHandlerFactory.class);
    private KeyStorage keyStorage;
    private UnitRegistry unitRegistry;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ItronBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(ItronBindingConstants.THING_TYPE_ITRON_SMOKE_DETECTOR)) {
            logger.debug("Creating handler for Itron device {}", thing.getUID().getId());
            return new ItronSmokeDetectorHandler(thing, new FilteredKeyStorage(keyStorage, thing), unitRegistry);
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
