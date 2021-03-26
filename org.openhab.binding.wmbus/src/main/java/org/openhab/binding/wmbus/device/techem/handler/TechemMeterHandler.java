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

package org.openhab.binding.wmbus.device.techem.handler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.config.DateFieldMode;
import org.openhab.binding.wmbus.device.techem.Record;
import org.openhab.binding.wmbus.device.techem.Record.Type;
import org.openhab.binding.wmbus.device.techem.TechemBindingConstants;
import org.openhab.binding.wmbus.device.techem.TechemDevice;
import org.openhab.binding.wmbus.device.techem.decoder.TechemFrameDecoder;
import org.openhab.binding.wmbus.handler.WMBusDeviceHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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

                    if (recordType.isDate()) {
                        String acceptedType = "";
                        Channel channel = getThing().getChannel(channelUID.getId());
                        if (channel != null) {
                            acceptedType = channel.getAcceptedItemType();
                        }
                        if (CoreItemFactory.DATETIME.equals(acceptedType)
                                && DateFieldMode.DATE_TIME == getDateFieldMode()) {
                            record.map(measurement -> map(measurement, measurement.getValue()))
                                    .ifPresent(state -> updateState(channelUID.getId(), state));
                        } else if (CoreItemFactory.STRING.equals(acceptedType)
                                && DateFieldMode.FORMATTED_STRING == getDateFieldMode()) {
                            record.map(measurement -> map(measurement, measurement.getValue()))
                                    .ifPresent(state -> updateState(channelUID.getId(), state));
                        } else if (CoreItemFactory.NUMBER.equals(acceptedType)
                                && DateFieldMode.UNIX_TIMESTAMP == getDateFieldMode()) {
                            record.map(measurement -> map(measurement, measurement.getValue()))
                                    .ifPresent(state -> updateState(channelUID.getId(), state));
                        } else {
                            logger.info(
                                    "Ignoring update of channel {}, it is date field with no proper mapping available.",
                                    channelUID);
                        }
                    } else {
                        record.map(measurement -> map(measurement, measurement.getValue()))
                                .ifPresent(state -> updateState(channelUID.getId(), state));
                    }

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
        State returnvalue = null; // maybe Undef would be better?
        if (value instanceof Quantity) {
            Quantity<?> quantity = (Quantity<?>) value;
            returnvalue = new QuantityType<>(quantity.getValue(), quantity.getUnit());
        } else if (value instanceof Integer) {
            returnvalue = new DecimalType(((Integer) value).floatValue());
        } else if (value instanceof Double) {
            returnvalue = new DecimalType(((Double) value).floatValue());
        } else if (value instanceof Float) {
            returnvalue = new DecimalType(((Float) value).floatValue());
        } else if (value instanceof LocalDateTime) {
            returnvalue = convertDate(
                    new DateTimeType(ZonedDateTime.of((LocalDateTime) value, ZoneId.systemDefault())));
        } else if (value instanceof ZonedDateTime) {
            returnvalue = convertDate(new DateTimeType((ZonedDateTime) value));
        }

        return returnvalue;
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
