/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    TechemWaterMeterFrameDecoder(Variant variant) {
        super(variant);
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

            return new TechemWaterMeter(device.getOriginalMessage(), device.getAdapter(), variant, records);
        }

        return null;
    }

}
