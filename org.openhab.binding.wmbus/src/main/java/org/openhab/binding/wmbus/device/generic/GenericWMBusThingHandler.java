/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.generic;

import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusDeviceHandler;
import org.openmuc.jmbus.DataRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Universal handler which covers all devices based on channel/record type mapping.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class GenericWMBusThingHandler<T extends WMBusDevice> extends WMBusDeviceHandler<T> {

    private final Logger logger = LoggerFactory.getLogger(GenericWMBusThingHandler.class);

    private final Map<String, RecordType> channelMapping;
    private final Map<RecordType, Unit<?>> unitMapping;

    protected GenericWMBusThingHandler(Thing thing, Map<String, RecordType> channelMapping,
            Map<RecordType, Unit<?>> unitMapping) {
        super(thing);
        this.channelMapping = channelMapping;
        this.unitMapping = unitMapping;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        logger.trace("Received command {} for channel {}", command, channelUID);
        if (command == RefreshType.REFRESH) {
            if (wmbusDevice != null) {
                RecordType recordType = channelMapping.get(channelUID.getId());
                if (recordType != null) {
                    DataRecord record = wmbusDevice.findRecord(recordType);

                    if (record != null) {
                        State newState = UnDefType.NULL;
                        Unit<?> unit = unitMapping.get(recordType);
                        if (unit != null) {
                            newState = new QuantityType<>(record.getScaledDataValue(), unit);
                        } else {
                            newState = convertRecordData(record);
                        }

                        logger.trace("Assigning new state {} to channel {}", newState, channelUID.getId());
                        updateState(channelUID.getId(), newState);
                    } else {
                        logger.warn("Could not read value of record {} in received frame", recordType);
                    }
                } else {
                    logger.warn("Unown channel {}, not supported by {}", channelUID, thing);
                }
            }
        }
    }

}
