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
package org.openhab.binding.wmbus.device.generic;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusDeviceHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
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

    private final UnitRegistry unitRegistry;
    private final Map<String, RecordType> channelMapping;

    protected GenericWMBusThingHandler(Thing thing, KeyStorage keyStorage, UnitRegistry unitRegistry,
            Map<String, RecordType> channelMapping) {
        super(thing, keyStorage);
        this.unitRegistry = unitRegistry;
        this.channelMapping = channelMapping;
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
                        State newState = unitRegistry.lookup(record.getUnit())
                                .map(unit -> new QuantityType<>(record.getScaledDataValue(), unit))
                                .map(State.class::cast).orElseGet(() -> convertRecordData(record));

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
