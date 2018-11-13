/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device;

import static org.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.handler.WMBusDeviceHandler;
import org.openmuc.jmbus.DataRecord;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/*
control field: 0x44
Secondary Address -> manufacturer ID: EFE, device ID: 64084076, device version: 0, device type: HEAT_METER, as bytes: C514764008640004
Variable Data Response:
VariableDataResponse has not been decoded. Bytes:
7A3A2000202F2F046D1A2F4C290407909E000001FD17000414190A2400043C38000000042C9A020000025F33000461EC0300
control field: 68, secondary address: manufacturer ID: EFE, device ID: 64084076, device version: 0, device type: HEAT_METER, as bytes: C514764008640004
access number: 58, status: 32, encryption mode: NONE, number of encrypted blocks: 0
record: DIB:04, VIB:6D -> descr:DATE_TIME, function:INST_VAL, value:Wed Sep 12 15:26:00 CEST 2018
record: DIB:04, VIB:07 -> descr:ENERGY, function:INST_VAL, scaled value:4.0592E8, value:40592.0, exponent:4, unit:WATT_HOUR, Wh
record: DIB:01, VIB:FD17 -> descr:ERROR_FLAGS, function:INST_VAL, value:0
record: DIB:04, VIB:14 -> descr:VOLUME, function:INST_VAL, scaled value:23618.81, value:2361881.0, exponent:-2, unit:CUBIC_METRE, m³
record: DIB:04, VIB:3C -> descr:VOLUME_FLOW, function:INST_VAL, scaled value:0.56, value:56.0, exponent:-2, unit:CUBIC_METRE_PER_HOUR, m³/h
record: DIB:04, VIB:2C -> descr:POWER, function:INST_VAL, scaled value:6660.0, value:666.0, exponent:1, unit:WATT, W
record: DIB:02, VIB:5F -> descr:RETURN_TEMPERATURE, function:INST_VAL, value:51, unit:DEGREE_CELSIUS, °C
record: DIB:04, VIB:61 -> descr:TEMPERATURE_DIFFERENCE, function:INST_VAL, scaled value:10.040000000000001, value:1004.0, exponent:-2, unit:KELVIN, S
*/

/**
 * The {@link EngelmannHeatMeter} class defines Engelmann Heat Meter device
 *
 * @author Hanno - Felix Wagner - Initial contribution
 */

@Component(service = { EngelmannHeatMeter.class }, property = { "thing.type.id=68EFE04",
        "thing.type.name=engelmann_heat_meter_v0" })
public class EngelmannHeatMeter extends Meter {

    @Activate
    protected void activate(Map<String, String> properties) {
        thingTypeName = THING_TYPE_NAME_ENGELMANN_SENSOSTAR;
        thingTypeId = "68EFE04";
        thingType = new ThingTypeUID(BINDING_ID, thingTypeName);
        supportedThingTypes = Sets.newHashSet(thingType);
    }

    public static class EngelmannHeatMeterHandler extends WMBusDeviceHandler {
        public static final Logger logger = LoggerFactory.getLogger(EngelmannHeatMeter.class);

        private final Map<String, RecordType> channels = new ImmutableMap.Builder<String, RecordType>()
                .put(CHANNEL_CURRENTDATE, new RecordType(0x04, 0x6D))
                .put(CHANNEL_CURRENTENERGYTOTAL, new RecordType(0x04, 0x07))
                .put(CHANNEL_ERRORFLAGS, new RecordType(0x01, 0xFD17))
                .put(CHANNEL_CURRENTVOLUMETOTAL, new RecordType(0x04, 0x14))
                .put(CHANNEL_CURRENTVOLUMEFLOW, new RecordType(0x04, 0x3C))
                .put(CHANNEL_CURRENTPOWER, new RecordType(0x04, 0x2C))
                .put(CHANNEL_RETURNTEMPERATURE, new RecordType(0x04, 0x5F))
                .put(CHANNEL_TEMPERATUREDIFFERENCE, new RecordType(0x04, 0x61)).build();

        public EngelmannHeatMeterHandler(Thing thing) {
            super(thing);
        }

        @Override
        public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
            logger.trace("handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: "
                    + command.toString());

            if (command == RefreshType.REFRESH) {
                logger.trace("handleCommand(): (2/5) command.refreshtype == REFRESH");
                State newState = UnDefType.NULL;
                if (wmbusDevice != null) {
                    logger.trace("handleCommand(): (3/5) deviceMessage != null");

                    RecordType recordType = channels.get(channelUID.getId());

                    if (recordType != null) {
                        logger.trace("handleCommand(): (4/5): got a valid channel: {} with matching RecordType {}",
                                channelUID.getId(), recordType);
                        DataRecord record = wmbusDevice.findRecord(recordType);
                        if (record != null) {
                            newState = convertRecordData(record);
                        } else {
                            logger.trace("handleCommand(): record not found in message");
                        }
                    } else {
                        logger.debug("handleCommand(): (4/5): no value for channel {} found", channelUID.getId());
                    }
                    logger.trace("handleCommand(): (5/5) assigning new state to channel '"
                            + channelUID.getId().toString() + "': " + newState.toString());
                    updateState(channelUID.getId(), newState);

                }

            }

        }

    }

}
