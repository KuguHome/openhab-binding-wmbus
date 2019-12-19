/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.Record;
import org.openhab.binding.wmbus.device.techem.TechemHeatMeter;
import org.openhab.binding.wmbus.device.techem.Variant;
import org.openmuc.jmbus.SecondaryAddress;

// TODO adjust after finding test frame
class TechemHeatMeterFrameDecoder extends AbstractTechemFrameDecoder<TechemHeatMeter> {

    private final Variant[] variants;

    TechemHeatMeterFrameDecoder(Variant ... variants) {
        super(variants[0]);
        this.variants = variants;
    }

    @Override
    protected TechemHeatMeter decode(WMBusDevice device, SecondaryAddress address, byte[] buffer) {
        int offset = address.asByteArray().length + 2;
        int coding = buffer[offset] & 0xFF;

        for (Variant variant : variants) {
            if (variant.getCoding() == coding) {
                LocalDateTime lastReading = parseLastDate(buffer, offset + 2);
                float lastValue = parseLastPeriod(buffer, offset + 4);
                LocalDateTime currentDate = parseCurrentDate(buffer, offset + 6);
                float currentValue = parseActualPeriod(buffer, offset + 8);

                List<Record<?>> records = new ArrayList<>();
                records.add(new Record<>(Record.Type.CURRENT_READING_DATE, currentDate));
                records.add(new Record<>(Record.Type.CURRENT_VOLUME, currentValue));
                records.add(new Record<>(Record.Type.PAST_VOLUME, lastValue));
                records.add(new Record<>(Record.Type.PAST_READING_DATE, lastReading));
                records.add(new Record<>(Record.Type.RSSI, device.getOriginalMessage().getRssi()));

                return new TechemHeatMeter(device.getOriginalMessage(), device.getAdapter(), variant, records);
            }
        }

        return null;
    }

    private float parseLastPeriod(byte[] buffer, int index) {
        byte[] value = read(buffer, index, index + 1, index + 2);

        return (value[2] & 0xFF) + ((value[1] & 0xFF) << 8) + ((value[0] & 0xFF) << 16);
    }

    private float parseActualPeriod(byte[] buffer, int index) {
        byte[] value = read(buffer, index, index + 1, index + 2);

        return (value[2] & 0xFF) + ((value[1] & 0xFF) << 8) + ((value[0] & 0xFF) << 16);
    }

    protected LocalDateTime parseActualDate(byte[] buffer, int dayIndex, int monthIndex) {
        int dateint = parseBigEndianInt(buffer, dayIndex);

        int day = (dateint >> 7) & 0x1F;
        int month = (buffer[monthIndex] >> 3) & 0x0F;

        LocalDateTime dateTime = LocalDateTime.now().withMonth(month).withDayOfMonth(day);
        return dateTime.truncatedTo(ChronoUnit.SECONDS);

    }

}
