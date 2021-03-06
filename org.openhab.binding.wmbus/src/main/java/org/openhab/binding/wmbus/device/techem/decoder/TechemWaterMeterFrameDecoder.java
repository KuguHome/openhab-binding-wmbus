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
package org.openhab.binding.wmbus.device.techem.decoder;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Unit;
import javax.measure.quantity.Volume;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.Record;
import org.openhab.binding.wmbus.device.techem.TechemWaterMeter;
import org.openhab.binding.wmbus.device.techem.Variant;
import org.openmuc.jmbus.SecondaryAddress;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

class TechemWaterMeterFrameDecoder extends AbstractTechemFrameDecoder<TechemWaterMeter> {

    /**
     * Offset of counter byte, if set to -1 means that there is no counter and history to be read.
     */
    private final int counterByteOffset;

    TechemWaterMeterFrameDecoder(Variant variant, int counterByteOffset) {
        super(variant);
        this.counterByteOffset = counterByteOffset;
    }

    @Override
    protected TechemWaterMeter decode(WMBusDevice device, SecondaryAddress address, byte[] buffer) {
        Buffer buff = new Buffer(device.getOriginalMessage(), address);

        int coding = buff.skip(2).readByte() & 0xFF;

        if (coding == variant.getCoding()) {
            Unit<Volume> unit = Units.CUBIC_METRE;

            List<Record<?>> records = new ArrayList<>();
            records.add(new Record<>(Record.Type.STATUS, ((Byte) buff.readByte()).intValue()));
            records.add(new Record<>(Record.Type.PAST_READING_DATE, buff.readPastDate()));
            float pastVolume = buff.readFloat(Buffer._SCALE_FACTOR_1_10th);
            records.add(new Record<>(Record.Type.PAST_VOLUME, Quantities.getQuantity(pastVolume, unit)));
            records.add(new Record<>(Record.Type.CURRENT_READING_DATE, buff.readCurrentDate()));
            float number = buff.readFloat(Buffer._SCALE_FACTOR_1_10th);
            records.add(new Record<>(Record.Type.CURRENT_VOLUME, Quantities.getQuantity(number, unit)));
            records.add(new Record<>(Record.Type.RSSI, device.getOriginalMessage().getRssi()));

            if (counterByteOffset >= 0) {
                int counter = buff.skip(counterByteOffset).readByte() & 0xFF;
                records.add(new Record<>(Record.Type.COUNTER, counter));
                records.add(new Record<>(Record.Type.ALMANAC, buff.readHistory()));
            }

            return new TechemWaterMeter(device.getOriginalMessage(), device.getAdapter(), variant, records);
        }

        return null;
    }
}
