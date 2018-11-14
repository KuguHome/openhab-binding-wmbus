/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device.techem.handler;

import static org.openhab.binding.wmbus.WMBusBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.TechemDevice;
import org.openhab.binding.wmbus.device.techem.TechemHKV;
import org.openhab.binding.wmbus.device.techem.decoder.TechemFrameDecoder;
import org.openhab.binding.wmbus.handler.WMBusDeviceHandler;
import org.openmuc.jmbus.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TechemHKVHandler} class defines TechemHKVHandler device
 *
 * @author Hanno - Felix Wagner - Initial contribution
 * @author Łukasz Dywicki - Added parsing of techem messages.
 */

public class TechemHKVHandler extends WMBusDeviceHandler<TechemHKV> {

    private final Logger logger = LoggerFactory.getLogger(TechemHKVHandler.class);
    private final TechemFrameDecoder<TechemDevice> decoder;

    public TechemHKVHandler(Thing thing, TechemFrameDecoder<TechemDevice> decoder) {
        super(thing);
        this.decoder = decoder;
    }

    @Override
    public void initialize(TechemHKV device) {
        updateStatus(ThingStatus.ONLINE);
    }

    // @Override
    // public void onChangedWMBusDevice(WMBusAdapter adapter, WMBusDevice receivedDevice) {
    // if (receivedDevice.getDeviceId().equals(deviceId)) {
    // wmbusDevice = receivedDevice;
    //
    // if (wmbusDevice instanceof TechemDevice) {
    // techemDevice = (TechemDevice) wmbusDevice;
    // triggerRefresh();
    // }
    // }
    // }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand {} for channel {}", command.toString(), channelUID.toString());
        if (command == RefreshType.REFRESH) {
            State newState = UnDefType.NULL;

            if (wmbusDevice != null) {
                switch (channelUID.getId()) {
                    case CHANNEL_ROOMTEMPERATURE: {
                        newState = new DecimalType(wmbusDevice.getT1());
                        break;
                    }
                    case CHANNEL_RADIATORTEMPERATURE: {
                        newState = new DecimalType(wmbusDevice.getT2());
                        break;
                    }
                    case CHANNEL_CURRENTREADING: {
                        newState = new DecimalType(wmbusDevice.getCurVal());
                        break;
                    }
                    case CHANNEL_LASTREADING: {
                        newState = new DecimalType(wmbusDevice.getLastVal());
                        break;
                    }
                    case CHANNEL_RECEPTION: {
                        newState = new DecimalType(wmbusDevice.getRssi());
                        break;
                    }
                    case CHANNEL_LASTDATE: {
                        newState = new DateTimeType(wmbusDevice.getLastDate());
                        break;
                    }
                    case CHANNEL_CURRENTDATE: {
                        newState = new DateTimeType(wmbusDevice.getCurDate());
                        break;
                    }
                    case CHANNEL_ALMANAC: {
                        newState = new StringType(wmbusDevice.getHistory());
                        break;
                    }
                    default: {
                        logger.debug("handleCommand(): channel {} unknown", channelUID.getId());
                        break;
                    }
                }
                if (newState != UnDefType.NULL) {
                    logger.trace("handleCommand(): assigning new state {} to channel {}", newState, channelUID.getId());
                    updateState(channelUID.getId(), newState);
                }
            }
        }
    }

    @Override
    protected TechemHKV parseDevice(WMBusDevice device) throws DecodingException {
        TechemHKV techemMessage = new TechemHKV(device.getOriginalMessage(), device.getAdapter());
        techemMessage.decode();
        return techemMessage;
    }
}
