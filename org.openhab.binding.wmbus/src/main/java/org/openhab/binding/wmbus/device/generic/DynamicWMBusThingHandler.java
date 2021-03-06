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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.handler.WMBusDeviceHandler;
import org.openhab.binding.wmbus.internal.WMBusChannelTypeProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.util.ThingHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.util.HexUtils;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.VariableDataStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Universal dynamic handler which covers devices based on dib/vib and channel type mapping.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class DynamicWMBusThingHandler<T extends WMBusDevice> extends WMBusDeviceHandler<T> {

    private static final String CHANNEL_PROPERTY_VIB = "vib";

    private static final String CHANNEL_PROPERTY_DIB = "dib";

    private final Logger logger = LoggerFactory.getLogger(DynamicWMBusThingHandler.class);

    private final UnitRegistry unitRegistry;
    private final WMBusChannelTypeProvider channelTypeProvider;

    public DynamicWMBusThingHandler(Thing thing, KeyStorage keyStorage, UnitRegistry unitRegistry,
            WMBusChannelTypeProvider channelTypeProvider) {
        super(thing, keyStorage);
        this.unitRegistry = unitRegistry;
        this.channelTypeProvider = channelTypeProvider;
    }

    @Override
    public void onChangedWMBusDevice(WMBusAdapter adapter, WMBusDevice receivedDevice) {
        if (receivedDevice.getDeviceAddress().equals(deviceAddress)) {
            VariableDataStructure response = receivedDevice.getOriginalMessage().getVariableDataResponse();

            List<Channel> channels = new ArrayList<>();
            for (DataRecord record : response.getDataRecords()) {
                Optional<ChannelTypeUID> typeId = WMBusChannelTypeProvider.getChannelType(record);
                Optional<Channel> channel = typeId.map(type -> thing.getChannel(type.getId()));

                if (typeId.isPresent() && !channel.isPresent()) {
                    Channel newChannel = createChannel(typeId.get(), record);
                    try {
                        ThingHelper.ensureUniqueChannels(channels, newChannel);
                        channels.add(newChannel);
                    } catch (IllegalArgumentException ex) {
                        logger.debug("Cannot create channel: {}", ex.getMessage());
                        // TODO instead of dropping, rename the duplicate with a suffix like _1,_2 etc
                    }
                }
            }

            if (!channels.isEmpty()) {
                ThingBuilder updatedThing = editThing().withChannels(channels);
                updateThing(updatedThing.build());
            }
        }

        super.onChangedWMBusDevice(adapter, receivedDevice);
    }

    private Channel createChannel(ChannelTypeUID typeId, DataRecord record) {
        ChannelType type = channelTypeProvider.getChannelType(typeId, null);

        ChannelBuilder channelBuilder = ChannelBuilder.create(new ChannelUID(thing.getUID(), typeId.getId()),
                type.getItemType());

        Map<String, String> properties = ImmutableMap.of(CHANNEL_PROPERTY_DIB, HexUtils.bytesToHex(record.getDib()),
                CHANNEL_PROPERTY_VIB, HexUtils.bytesToHex(record.getVib()));

        channelBuilder.withType(typeId).withProperties(properties).withLabel(type.getLabel());

        String description = type.getDescription();
        if (description != null) {
            channelBuilder.withDescription(description);
        }

        return channelBuilder.build();
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        logger.trace("Received command {} for channel {}", command, channelUID);
        if (wmbusDevice != null && command == RefreshType.REFRESH) {
            Optional<Map<String, String>> properties = Optional.ofNullable(thing.getChannel(channelUID.getId()))
                    .map(ch -> ch.getProperties());

            Optional<byte[]> dib = properties.map(map -> map.get(CHANNEL_PROPERTY_DIB)).map(HexUtils::hexToBytes);
            Optional<byte[]> vib = properties.map(map -> map.get(CHANNEL_PROPERTY_VIB)).map(HexUtils::hexToBytes);

            if (dib.isPresent() && vib.isPresent()) {
                RecordType recordType = new RecordType(dib.get(), vib.get());
                DataRecord record = wmbusDevice.findRecord(recordType);

                if (record != null) {
                    State newState = unitRegistry.lookup(record.getUnit())
                            .map(unit -> new QuantityType<>(record.getScaledDataValue(), unit)).map(State.class::cast)
                            .orElseGet(() -> convertRecordData(record));

                    logger.trace("Assigning new state {} to channel {}", newState, channelUID.getId());
                    updateState(channelUID.getId(), newState);
                } else {
                    logger.warn("Could not read value of record {} in received frame", recordType);
                }
            } else {
                logger.warn("Unknown channel {}, not supported by {}", channelUID, thing);
            }
        }
    }
}
