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
import java.util.function.Function;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.TechemDevice;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusMessage;

public abstract class AbstractTechemFrameDecoder<T extends TechemDevice> implements TechemFrameDecoder<T> {

    private final String variant;

    protected final Function<Float, Float> _SCALE_FACTOR_1_10th = value -> value / 10;
    protected final Function<Float, Float> _SCALE_FACTOR_1_100th = value -> value / 100;

    protected AbstractTechemFrameDecoder(String variant) {
        this.variant = variant;
    }

    @Override
    public final boolean suports(String deviceVariant) {
        return variant.equals(deviceVariant);
    }

    protected final byte[] read(byte[] buffer, int... indexes) {
        byte[] value = new byte[indexes.length];
        for (int element = 0; element < indexes.length; element++) {
            value[element] = buffer[indexes[element]];
        }
        return value;
    }

    protected final int parseBigEndianInt(byte[] buffer, int index) {
        return parseBigEndianInt(buffer, index, index + 1);
    }

    protected final int parseBigEndianInt(byte[] buffer, int lsb, int msb) {
        byte[] value = read(buffer, lsb, msb);
        return (value[0] & 0xFF) + ((value[1] & 0xFF) << 8);
    }

    protected final float parseValue(byte[] buffer, int index, Function<Float, Float> scale) {
        float value = parseBigEndianInt(buffer, index);

        return scale.apply(value);
    }

    protected final LocalDateTime parseLastDate(byte[] buffer, int index) {
        int dateint = parseBigEndianInt(buffer, index);

        int day = (dateint >> 0) & 0x1F;
        int month = (dateint >> 5) & 0x0F;
        int year = (dateint >> 9) & 0x3F;

        LocalDateTime dateTime = LocalDateTime.of(2000 + year, month, day, 0, 0, 0, 0);
        return dateTime.truncatedTo(ChronoUnit.DAYS);
    }

    protected final LocalDateTime parseCurrentDate(byte[] buffer, int index) {
        int dateint = parseBigEndianInt(buffer, index);

        int day = (dateint >> 4) & 0x1F;
        int month = (dateint >> 9) & 0x0F;

        LocalDateTime dateTime = LocalDateTime.now().withMonth(month).withDayOfMonth(day);
        return dateTime.truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public T decode(WMBusDevice device) {
        WMBusMessage message = device.getOriginalMessage();
        return decode(device, message.getSecondaryAddress(), message.asBlob());
    }

    // FIXME make this method abstract
    protected abstract T decode(WMBusDevice device, SecondaryAddress address, byte[] buffer);

}
