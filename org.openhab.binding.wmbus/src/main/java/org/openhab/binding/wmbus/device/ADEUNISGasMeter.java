/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
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
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link ADEUNISGasMeter} class defines ADEUNIS Gas Meter device
 *
 * @author Roman Malyugin - Initial contribution
 */

@Component(service = { ADEUNISGasMeter.class }, property = { "volume.dib=4", "volume.vib=18",
        "channel.volume=current_volume_total_m3", "binding.id=wmbus", "device.ids=10011104,20011104",
        "thing.type.id=68ARF33", "thing.type.name=adeunis_rf_gas_meter_v3" })
public class ADEUNISGasMeter extends Meter {

    public static final Logger logger = LoggerFactory.getLogger(ADEUNISGasMeter.class);

    public static String CHANNEL_CURRENT_VOLUME_INST_VAL;

    private static RecordType TYPE_CURRENT_VOLUME_INST_VAL;

    private int dib;
    private int vib;

    @Activate
    protected void activate(Map<String, String> properties) {
        this.dib = Integer.valueOf(properties.get("volume.dib"));
        this.vib = Integer.valueOf(properties.get("volume.vib"));
        TYPE_CURRENT_VOLUME_INST_VAL = new RecordType(dib, vib);
        thingTypeName = properties.get("thing.type.name");
        thingTypeId = properties.get("thing.type.id");
        thingType = new ThingTypeUID(properties.get("binding.id"), thingTypeName);
        supportedThingTypes = Sets.newHashSet(thingType);
        CHANNEL_CURRENT_VOLUME_INST_VAL = properties.get("channel.volume");
    }

    @Deactivate
    protected void deactivate() {
    }

    public static class ADEUNISGasMeterHandler extends WMBusDeviceHandler {

        public ADEUNISGasMeterHandler(Thing thing) {
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
                    if (CHANNEL_CURRENT_VOLUME_INST_VAL.equals(channelUID.getId())) {
                        logger.trace("handleCommand(): (4/5): got a valid channel: VOLUME_INST_VAL");
                        DataRecord record = wmbusDevice.findRecord(TYPE_CURRENT_VOLUME_INST_VAL);
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("handleCommand(): record not found in message");
                        }
                    } else {
                        logger.debug("handleCommand(): (4/5): no channel to put this value into found: "
                                + channelUID.getId());
                    }
                    logger.trace("handleCommand(): (5/5) assigning new state to channel '"
                            + channelUID.getId().toString() + "': " + newState.toString());
                    updateState(channelUID.getId(), newState);

                }

            }

        }

    }
}
