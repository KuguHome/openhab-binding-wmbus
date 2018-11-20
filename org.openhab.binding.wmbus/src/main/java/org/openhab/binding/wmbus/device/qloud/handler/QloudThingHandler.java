/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.qloud.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.generic.GenericWMBusThingHandler;
import org.openhab.binding.wmbus.device.qloud.QloudBindingConstants;
import org.openhab.binding.wmbus.device.qloud.QloudWMBusDevice;
import org.openmuc.jmbus.DecodingException;

/**
 * Universal handler which covers all Q-loud configurations based on channel/record type mapping.
 *
 * @author Łukasz Dywicki - Initial contribution/
 */
public class QloudThingHandler extends GenericWMBusThingHandler<QloudWMBusDevice> {

    public QloudThingHandler(Thing thing) {
        this(thing, fetchMapping(thing.getThingTypeUID()));
    }

    protected QloudThingHandler(Thing thing, Map<String, RecordType> channelMapping) {
        super(thing, channelMapping, QloudBindingConstants.RECORD_UNITS);
    }

    @Override
    protected QloudWMBusDevice parseDevice(WMBusDevice device) throws DecodingException {
        return new QloudWMBusDevice(device);
    }

    private static Map<String, RecordType> fetchMapping(@NonNull ThingTypeUID thingTypeUID) {
        return QloudBindingConstants.RECORD_MAP.get(thingTypeUID);
    }

}
