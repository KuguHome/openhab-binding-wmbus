/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device.techem.handler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.Record;
import org.openhab.binding.wmbus.device.techem.Record.Type;
import org.openhab.binding.wmbus.device.techem.TechemBindingConstants;
import org.openhab.binding.wmbus.device.techem.TechemDevice;
import org.openhab.binding.wmbus.device.techem.decoder.TechemFrameDecoder;
import org.openhab.binding.wmbus.handler.WMBusDeviceHandler;
import org.openmuc.jmbus.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TechemMeterHandler} holds logic related to retrieval (actually mapping) of data reported by various techem
 * devices.
 *
 * @author Łukasz Dywicki - Initial contribution
 */

public class TechemMeterHandler<T extends TechemDevice> extends WMBusDeviceHandler<T> {

    private final Logger logger = LoggerFactory.getLogger(TechemMeterHandler.class);

    private final Class<T> type;
    private final TechemFrameDecoder<TechemDevice> decoder;
    private final Map<String, Type> channelMapping;

    public TechemMeterHandler(Thing thing, Class<T> type, TechemFrameDecoder<TechemDevice> decoder) {
        this(thing, type, decoder, fetchMapping(thing.getThingTypeUID()));
    }

    protected TechemMeterHandler(Thing thing, Class<T> type, TechemFrameDecoder<TechemDevice> decoder,
            Map<String, Type> channelMapping) {
        super(thing);
        this.type = type;
        this.decoder = decoder;
        this.channelMapping = channelMapping;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand {} for channel {}", command.toString(), channelUID.toString());
        if (command == RefreshType.REFRESH) {
            if (wmbusDevice != null) {
                Type recordType = channelMapping.get(channelUID.getId());
                if (recordType != null) {
                    Optional<Record<?>> record = wmbusDevice.getRecord(recordType);

                    record.map(measurement -> map(measurement, measurement.getValue()))
                            .ifPresent(state -> updateState(channelUID.getId(), state));

                    if (!record.isPresent()) {
                        logger.warn("Could not read value of record {} in received frame", recordType);
                    }
                } else {
                    logger.warn("Unown channel {}, not supported by {}", channelUID, thing.getUID());
                }
            }
        }
    }

    private State map(Record<?> measurement, Object value) {
        if (value instanceof Quantity) {
            Quantity<?> quantity = (Quantity<?>) value;
            return new QuantityType<>(quantity.getValue(), quantity.getUnit());
        } else if (value instanceof Integer) {
            return new DecimalType(((Integer) value).floatValue());
        } else if (value instanceof Double) {
            return new DecimalType(((Double) value).floatValue());
        } else if (value instanceof Float) {
            return new DecimalType(((Float) value).floatValue());
        } else if (value instanceof LocalDateTime) {
            return new DateTimeType(ZonedDateTime.of((LocalDateTime) value, ZoneId.systemDefault()));
        } else if (value instanceof ZonedDateTime) {
            return new DateTimeType((ZonedDateTime) value);
        }

        // maybe Undef would be better?
        return null;
    }

    @Override
    protected T parseDevice(WMBusDevice device) throws DecodingException {
        TechemDevice decodedDevice = decoder.decode(device);
        if (type.isInstance(decodedDevice)) {
            return type.cast(decodedDevice);
        }
        return null;
    }

    private static Map<String, Type> fetchMapping(@NonNull ThingTypeUID thingTypeUID) {
        return TechemBindingConstants.RECORD_MAP.get(thingTypeUID);
    }

}
