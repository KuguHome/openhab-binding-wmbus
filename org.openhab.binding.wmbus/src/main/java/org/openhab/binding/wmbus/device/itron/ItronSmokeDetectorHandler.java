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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.generic.GenericWMBusThingHandler;
import org.openhab.binding.wmbus.device.techem.decoder.Buffer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

public class ItronSmokeDetectorHandler extends GenericWMBusThingHandler<WMBusDevice> {

    public static final RecordType CURRENT_DATE_06_6D = new RecordType(0x06, 0x6D);
    public static final RecordType CONFIG_STATUS_07_7F = new RecordType(0x07, 0x7F);

    private final Logger logger = LoggerFactory.getLogger(ItronSmokeDetectorHandler.class);
    private Map<String, Object> parsedFrame = new HashMap<>();

    public ItronSmokeDetectorHandler(Thing thing, KeyStorage keyStorage, UnitRegistry unitRegistry) {
        super(thing, keyStorage, unitRegistry, Collections.emptyMap());
    }

    @Override
    protected WMBusDevice parseDevice(WMBusDevice device) throws DecodingException {
        WMBusDevice parsedDevice = super.parseDevice(device);

        DataRecord record = device.findRecord(CONFIG_STATUS_07_7F);
        if (record != null && record.getDataValueType() == DataRecord.DataValueType.LONG
                && record.getDescription() == DataRecord.Description.MANUFACTURER_SPECIFIC) {
            Long dataValue = (Long) record.getDataValue();
            ItronConfigStatusDataParser configStatus = new ItronConfigStatusDataParser(Longs.toByteArray(dataValue));

            // first (MSB) byte with billing date
            parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_BILLING_DATE, configStatus.getBillingDate());

            // second byte with status codes
            parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_REMOVAL_OCCURRED, configStatus.isRemovalOccurred());
            parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_PRODUCT_INSTALLED, configStatus.isProductInstalled());
            parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_OPERATION_MODE, configStatus.getOperationMode());
            parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_PERIMETER_INTRUSION_OCCURRED,
                    configStatus.isPerimeterIntrusionOccurred());
            parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_SMOKE_INLET_BLOCKED_OCCURRED,
                    configStatus.isSmokeInletBlockedOccurred());
            parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_OUT_OF_TEMP_RANGE_OCCURRED,
                    configStatus.isOutOfRangeTemperatureOccurred());

            // third byte with error codes
            parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_PRODUCT_CODE,
                    HexUtils.byteToHex(configStatus.getProductCode()));

            // fourth byte with battery lifetime
            parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_BATTERY_LIFETIME, configStatus.getBatteryLifetime());
        }

        // fifth and sixth byte SD errors
        // parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_PERIMETER_INTRUSION, configStatus.readBytes(2));

        // 7tn byte is modem error codes
        // parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_REMOVAL_ERROR, configStatus.readByte());

        // 8tn byte is config byte
        // parsedFrame.put(ItronBindingConstants.CHANNEL_STATUS_DATA_ENCRYPTED, configStatus.readByte());

        byte[] manufacturerData = parsedDevice.getOriginalMessage().getVariableDataResponse().getManufacturerData();

        Buffer buffer = new Buffer(manufacturerData);
        ItronManufacturerDataParser parser = new ItronManufacturerDataParser(buffer);

        LocalDateTime eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_START_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_START_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_START_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_END_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_END_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_END_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_BEEPER_STOPPED_DURING_SMOKE_ALERT_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_BEEPER_STOPPED_DURING_SMOKE_ALERT_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_BEEPER_STOPPED_DURING_SMOKE_ALERT_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_OCCURRED_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_OCCURRED_DATE_NUMBER,
                eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_OCCURRED_DATE_STRING,
                eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_REMOVED_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_REMOVED_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_REMOVED_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKED_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKED_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKED_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKING_REMOVED_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKING_REMOVED_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKING_REMOVED_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEMPERATURE_OUT_OF_RANGE_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEMPERATURE_OUT_OF_RANGE_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEMPERATURE_OUT_OF_RANGE_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEST_SWITCH_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEST_SWITCH_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEST_SWITCH_DATE_STRING, eventDate);

        parsedFrame.put(ItronBindingConstants.CHANNEL_NUMBER_OF_TEST_SWITCHES_OPERATED, buffer.readShort());
        parsedFrame.put(ItronBindingConstants.CHANNEL_PERIMETER_INTRUSION_DAY_COUNTER_CUMULATED, buffer.readShort());
        parsedFrame.put(ItronBindingConstants.CHANNEL_SMOKE_INLET_DAY_COUNTER_CUMULATED,
                buffer.available() < 2 ? buffer.readByte() : buffer.readShort());

        return parsedDevice;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (parsedFrame.containsKey(channelUID.getId())) {
            // channel directly maps to manufacturer data appended to frame
            logger.debug("Mapping custom smoke detector channel {} to manufacturer data", channelUID);

            Object value = parsedFrame.get(channelUID.getId());
            if (value == null) {
                updateState(channelUID, UnDefType.NULL);
            } else if (value instanceof LocalDateTime) {
                updateState(channelUID, convertDate(value));
            } else if (value instanceof Number) {
                updateState(channelUID, new DecimalType(((Number) value).floatValue()));
            } else if (value instanceof Boolean) {
                updateState(channelUID, ((boolean) value) ? OnOffType.ON : OnOffType.OFF);
            } else {
                logger.warn("Unsupported value type {}", value);
            }
        } else {
            // try to do a lookup based on channel to record mapping
            super.handleCommand(channelUID, command);
        }
    }
}
