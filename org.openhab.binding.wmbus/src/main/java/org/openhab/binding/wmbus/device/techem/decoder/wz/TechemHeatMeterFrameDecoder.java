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
import java.time.temporal.ChronoUnit;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.TechemHeatMeter;
import org.openmuc.jmbus.SecondaryAddress;

// TODO adjust after finding test frame
class TechemHeatMeterFrameDecoder extends AbstractTechemWZFrameDecoder<TechemHeatMeter> {

    TechemHeatMeterFrameDecoder() {
    }

    @Override
    protected TechemHeatMeter decode(WMBusDevice device, SecondaryAddress address, byte[] buffer) {
        int offset = address.asByteArray().length + 2;
        int coding = buffer[offset] & 0xFF;

        if (coding == 0xA2) {
            /*
             * LocalDateTime lastReading = parseLastDate(buffer, offset + 2);
             * float lastValue = parseValue(buffer, offset + 4);
             * LocalDateTime currentDate = parseCurrentDate(buffer, offset + 6);
             * float currentValue = parseValue(buffer, offset + 8);
             *
             * Unit<Volume> unit = Units.CUBIC_METRE;
             * Quantity<Volume> currentVolume = Quantities.getQuantity(currentValue, unit);
             * Quantity<Volume> pastVolume = Quantities.getQuantity(lastValue, unit);
             *
             * List<Record> records = new ArrayList<>();
             * records.add(new Record(Record.Type.CURRENT_VOLUME, currentVolume, currentDate));
             * records.add(new Record(Record.Type.PAST_VOLUME, pastVolume, lastReading));
             *
             * return new TechemHeatMeter(message, null, records);
             */
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

    private LocalDateTime parseActualDate(byte[] buffer, int dayIndex, int monthIndex) {
        int dateint = parseBigEndianInt(buffer, dayIndex);

        int day = (dateint >> 7) & 0x1F;
        int month = (buffer[monthIndex] >> 3) & 0x0F;

        LocalDateTime dateTime = LocalDateTime.now().withMonth(month).withDayOfMonth(day);
        return dateTime.truncatedTo(ChronoUnit.SECONDS);

    }

}
