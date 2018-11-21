/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder.wz;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Volume;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.Record;
import org.openhab.binding.wmbus.device.techem.TechemWaterMeter;
import org.openmuc.jmbus.DeviceType;
import org.openmuc.jmbus.SecondaryAddress;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

class TechemWaterMeterFrameDecoder extends AbstractTechemWZFrameDecoder<TechemWaterMeter> {

    private final DeviceType deviceType;
    private final byte variant;

    public TechemWaterMeterFrameDecoder(byte variant, DeviceType deviceType) {
        this.variant = variant;
        this.deviceType = deviceType;
    }

    @Override
    protected TechemWaterMeter decode(WMBusDevice device, SecondaryAddress address, byte[] buffer) {
        int offset = address.asByteArray().length + 2;
        int coding = buffer[offset] & 0xFF;

        if (coding == 0xA2) {
            LocalDateTime lastReading = parseLastDate(buffer, offset + 2);
            float lastValue = parseValue(buffer, offset + 4, _SCALE_FACTOR_1_10th);
            LocalDateTime currentDate = parseCurrentDate(buffer, offset + 6);
            float currentValue = parseValue(buffer, offset + 8, _SCALE_FACTOR_1_10th);

            Unit<Volume> unit = Units.CUBIC_METRE;
            Quantity<Volume> currentVolume = Quantities.getQuantity(currentValue, unit);
            Quantity<Volume> pastVolume = Quantities.getQuantity(lastValue, unit);

            List<Record<?>> records = new ArrayList<>();
            records.add(new Record<>(Record.Type.CURRENT_VOLUME, currentVolume));
            records.add(new Record<>(Record.Type.CURRENT_READING_DATE, currentDate));
            records.add(new Record<>(Record.Type.PAST_VOLUME, pastVolume));
            records.add(new Record<>(Record.Type.PAST_READING_DATE, lastReading));
            records.add(new Record<>(Record.Type.RSSI, device.getOriginalMessage().getRssi()));

            return new TechemWaterMeter(device.getOriginalMessage(), device.getAdapter(), deviceType, records);
        }

        return null;
    }

}
