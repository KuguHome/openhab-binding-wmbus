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
import java.util.HashMap;
import java.util.List;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.Record;
import org.openhab.binding.wmbus.device.techem.TechemHeatMeter;
import org.openhab.binding.wmbus.device.techem.Variant;
import org.openmuc.jmbus.SecondaryAddress;

class TechemHeatMeterFrameDecoder extends AbstractTechemFrameDecoder<TechemHeatMeter> {

    private final Variant[] variants;

    // lastDate,lastReading,currentDateDay,currentDateMonth,currentReading
    int[] fields22 = { 2, 3, 12, 7, 8 };
    int[] fields39 = { 2, 3, 12, 7, 9 };
    int[] fields57 = { 2, 3, 12, 7, 8 }; // Kamstrup Multical 402
    int[] fields71 = { 2, 3, 11, 7, 8 };

    HashMap<Integer, int[]> versionFieldMap = new HashMap<Integer, int[]>();

    TechemHeatMeterFrameDecoder(Variant... variants) {

        super(variants[0]);

        versionFieldMap.put(0x22, fields22);
        versionFieldMap.put(0x39, fields39);
        versionFieldMap.put(0x57, fields57);
        versionFieldMap.put(0x71, fields71);

        this.variants = variants;
    }

    @Override
    protected TechemHeatMeter decode(WMBusDevice device, SecondaryAddress address, byte[] buffer) {
        int offset = address.asByteArray().length + 2;
        int coding = buffer[offset] & 0xFF;

        for (Variant variant : variants) {
            int[] fieldOffset = versionFieldMap.get(variant.getVersion());
            if (variant.getCoding() == coding) {
                LocalDateTime lastDate = parseLastDate(buffer, offset + fieldOffset[0]);
                float lastReading = parseReading(buffer, offset + fieldOffset[1]);
                LocalDateTime currentDate = parseCurrentDate(buffer, offset + fieldOffset[2], offset + fieldOffset[3]);
                float currentReading = parseReading(buffer, offset + fieldOffset[4]);

                List<Record<?>> records = new ArrayList<>();
                records.add(new Record<>(Record.Type.CURRENT_READING_DATE, currentDate));
                records.add(new Record<>(Record.Type.CURRENT_READING, currentReading));
                records.add(new Record<>(Record.Type.PAST_READING, lastReading));
                records.add(new Record<>(Record.Type.PAST_READING_DATE, lastDate));
                records.add(new Record<>(Record.Type.RSSI, device.getOriginalMessage().getRssi()));

                return new TechemHeatMeter(device.getOriginalMessage(), device.getAdapter(), variant, records);
            }
        }

        return null;
    }

    private float parseReading(byte[] buffer, int index) {
        byte[] value = read(buffer, index, index + 1, index + 2);

        return (value[2] & 0xFF) + ((value[1] & 0xFF) << 8) + ((value[0] & 0xFF) << 16);
    }

    protected LocalDateTime parseCurrentDate(byte[] buffer, int dayIndex, int monthIndex) {
        int dateint = parseBigEndianInt(buffer, dayIndex);

        int day = (dateint >> 7) & 0x1F;
        int month = (buffer[monthIndex] >> 3) & 0x0F;

        LocalDateTime dateTime = LocalDateTime.now().withMonth(month).withDayOfMonth(day);
        return dateTime.truncatedTo(ChronoUnit.SECONDS);

    }

}
