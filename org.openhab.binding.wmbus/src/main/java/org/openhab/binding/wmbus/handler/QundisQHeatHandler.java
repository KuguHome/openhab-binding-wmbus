/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.handler;

import static org.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.util.Set;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DataRecord.DataValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link QundisQHeatHandler} class defines Device/thing handler for the Qundis Qheat 5 water heat meter
 * (Wärmemengenzähler)
 *
 * @author Hanno - Felix Wagner - Initial contribution
 */

public class QundisQHeatHandler extends WMBusDeviceHandler {

    // set this when adding new device handler
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_QUNDIS_QHEAT_5);
    private final Logger logger = LoggerFactory.getLogger(QundisQHeatHandler.class);

    public QundisQHeatHandler(Thing thing, KeyStorage keyStorage) {
        super(thing, keyStorage);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: "
                + command.toString());
        if (command == RefreshType.REFRESH) {
            logger.trace("handleCommand(): (2/5) command.refreshtype == REFRESH");
            State newState = UnDefType.NULL;

            // TODO manche VIB-DIB-Kombinationen wiederholen sich -> siehe JMbus-Bibliothek-Dokumentation betreffend
            // DataRecord, wie das kodiert ist
            // -> ev. Verallgemeinerung auf 1 Klasse je Typ möglich (WATER_METER, HEAT_COST_ALLOCATOR, HEAT_METER usw.)

            if (wmbusDevice != null) {
                logger.trace("handleCommand(): (3/5) deviceMessage != null");
                // TODO make own channel type for energy_Wh
                /*
                 * DIB:0x04, VIB:0x6d -> descr:DATE_TIME, function:INST_VAL, value:Wed Nov 15 17:49:00 CET 2017 --
                 * current reading (timestamp)
                 * DIB:0x0c, VIB:0x05 -> descr:ENERGY, function:INST_VAL, scaled value:0.0, unit:WATT_HOUR, Wh --
                 * current reading (measurement)
                 * DIB:0x0d, VIB:0xff 0x5f -> descr:MANUFACTURER_SPECIFIC, function:INST_VAL, value:*?,??4? -- currently
                 * not interpreted; also used during installation messages
                 * DIB:0x02, VIB:0xfd 0x17 -> descr:ERROR_FLAGS, function:INST_VAL, value:0 -- numeric (?) value of
                 * current error
                 * DIB:0x0c, VIB:0x78 -> descr:FABRICATION_NO, function:INST_VAL, value:56725702 -- currently not
                 * interpreted; is not equal to the WMBus device ID
                 */
                switch (channelUID.getId()) {
                    case CHANNEL_RECEPTION: {
                        logger.trace("handleCommand(): (4/5): got a valid channel: RECEPTION");
                        newState = new DecimalType(wmbusDevice.getOriginalMessage().getRssi());
                        break;
                    }
                    // TODO do not instantiate new byte arrays each time (re-use / convert to constants)
                    case CHANNEL_CURRENTREADING: {
                        logger.trace("handleCommand(): (4/5): got a valid channel: CURRENTREADING");
                        DataRecord record = wmbusDevice.findRecord(new byte[] { 0x0c }, new byte[] { 0x05 });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("handleCommand(): record not found in message");
                        }
                        break;
                    }
                    case CHANNEL_CURRENTDATE: {
                        logger.trace("handleCommand(): (4/5): got a valid channel: CURRENTDATE");
                        DataRecord record = wmbusDevice.findRecord(new byte[] { 0x04 }, new byte[] { 0x6d }); // TODO
                                                                                                              // same as
                                                                                                              // for
                                                                                                              // Qcaloric
                                                                                                              // 5,5 and
                                                                                                              // Qwater
                                                                                                              // 5,5
                        if (record != null && record.getDataValueType() == DataValueType.DATE) {
                            newState = convertDate(record.getDataValue());
                        } else {
                            logger.trace("handleCommand(): record not found in message or not of type date");
                        }
                        break;
                    }
                    case CHANNEL_ERRORFLAGS: { // this is a current value / inst_val
                        logger.trace("handleCommand(): (4/5): got a valid channel: ERRORFLAGS");
                        DataRecord record = wmbusDevice.findRecord(new byte[] { 0x02 },
                                new byte[] { (byte) 0xFD, 0x17 });
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("handleCommand(): record not found in message");
                        }
                        break;
                    }
                    default:
                        logger.debug("handleCommand(): (4/5): no channel to put this value into found: "
                                + channelUID.getId());
                        break;
                }
                logger.trace("handleCommand(): (5/5) assigning new state to channel '" + channelUID.getId().toString()
                        + "': " + newState.toString());
                updateState(channelUID.getId(), newState);
            }
        }
    }

    @Override
    boolean checkMessage(WMBusDevice receivedDevice) {
        // in between the standard OMS format messages, this device sends out messages in manufcaturer-specific format
        // in between -> filter these out
        // these messages are missing the usual values -> would lead to channels being set to NULL
        /*
         * TODO check does this device also send these manufacturer-specific messages?
         * DIB:0D, VIB:FF5F -> descr:MANUFACTURER_SPECIFIC, function:INST_VAL, value:/
         * DIB:04, VIB:6D -> descr:DATE_TIME, function:INST_VAL, value:Mon Mar 05 12:33:00 CET 2018
         * (no other records in these messages)
         */
        // TODO why is type cast at 0xff required? 0xff already is a byte, right?
        DataRecord record = receivedDevice.findRecord(new byte[] { 0x0d }, new byte[] { (byte) 0xff, 0x5f });
        if (record != null) {
            logger.trace(
                    "onChangedWMBusDevice(): this is a message with non-OMS manufacturer-specific value, ignoring this message");
            return false;
        }
        return true;
    }
}
