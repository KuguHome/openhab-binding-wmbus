/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.diehl.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.diehl.DiehlBindingConstants;
import org.openhab.binding.wmbus.device.diehl.DiehlWMBusDevice;
import org.openhab.binding.wmbus.device.generic.GenericWMBusThingHandler;
import org.openmuc.jmbus.DecodingException;

/**
 * Universal handler which covers all Diehl based on channel/record type mapping.
 *
 * @author Łukasz Dywicki - Initial contribution/
 */
public class DiehlThingHandler extends GenericWMBusThingHandler<DiehlWMBusDevice> {

    public DiehlThingHandler(Thing thing) {
        this(thing, fetchMapping(thing.getThingTypeUID()));
    }

    protected DiehlThingHandler(Thing thing, Map<String, RecordType> channelMapping) {
        super(thing, channelMapping, DiehlBindingConstants.RECORD_UNITS);
    }

    @Override
    protected DiehlWMBusDevice parseDevice(WMBusDevice device) throws DecodingException {
        return new DiehlWMBusDevice(device);
    }

    private static Map<String, RecordType> fetchMapping(@NonNull ThingTypeUID thingTypeUID) {
        return DiehlBindingConstants.RECORD_MAP.get(thingTypeUID);
    }

}
