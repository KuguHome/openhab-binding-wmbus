/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.landisgyr.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.generic.GenericWMBusThingHandler;
import org.openhab.binding.wmbus.device.landisgyr.LandisGyrBindingConstants;
import org.openhab.binding.wmbus.device.landisgyr.LandisGyrWMBusDevice;
import org.openmuc.jmbus.DecodingException;

/**
 * Universal handler which covers all Landis+Gyr based on channel/record type mapping.
 *
 * @author Łukasz Dywicki - Initial contribution/
 */
public class LandisGyrThingHandler extends GenericWMBusThingHandler<LandisGyrWMBusDevice> {

    public LandisGyrThingHandler(Thing thing, UnitRegistry unitRegistry) {
        this(thing, unitRegistry, fetchMapping(thing.getThingTypeUID()));
    }

    protected LandisGyrThingHandler(Thing thing, UnitRegistry unitRegistry, Map<String, RecordType> channelMapping) {
        super(thing, unitRegistry, channelMapping);
    }

    @Override
    protected LandisGyrWMBusDevice parseDevice(WMBusDevice device) throws DecodingException {
        return new LandisGyrWMBusDevice(device);
    }

    private static Map<String, RecordType> fetchMapping(@NonNull ThingTypeUID thingTypeUID) {
        return LandisGyrBindingConstants.RECORD_MAP.get(thingTypeUID);
    }

}
