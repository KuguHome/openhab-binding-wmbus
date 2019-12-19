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

import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.TechemDevice;
import org.openhab.binding.wmbus.device.techem.Variant;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractTechemFrameDecoder<T extends TechemDevice> implements TechemFrameDecoder<T> {

    private final Logger logger = LoggerFactory.getLogger(AbstractTechemFrameDecoder.class);
    protected final Variant variant;

    protected final Function<Float, Float> _SCALE_FACTOR_1_10th = value -> value / 10;
    protected final Function<Float, Float> _SCALE_FACTOR_1_100th = value -> value / 100;

    protected AbstractTechemFrameDecoder(Variant variant) {
        this.variant = variant;
    }

    @Override
    public final boolean supports(String deviceVariant) {
        boolean supports = variant.getRawType().equals(deviceVariant);
        logger.debug("Does decoder {} support meter variant {}? {}", variant, deviceVariant, supports);
        return supports;
    }

    protected final byte[] read(byte[] buffer, int... indexes) {
        byte[] value = new byte[indexes.length];
        for (int element = 0; element < indexes.length; element++) {
            value[element] = buffer[indexes[element]];
        }
        return value;
    }

    protected final int parseBigEndianInt(byte[] buffer, int index) {
        if (buffer.length < index + 1) {
            return 0x00;
        }
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

        if (day <= 0) {
            logger.trace("Detected invalid day number {} in byte representation: {}, changing to 1st day of month", day,
                    HexUtils.bytesToHex(read(buffer, index, index + 1)));
            day = 1;
        }
        if (month <= 0) {
            logger.trace("Detected invalid month number {} in byte representation: {}, changing to last month of year",
                    month, HexUtils.bytesToHex(read(buffer, index, index + 1)));
            month = 12;
        }

        LocalDateTime dateTime = LocalDateTime.now().withMonth(month).withDayOfMonth(day);
        return dateTime.truncatedTo(ChronoUnit.SECONDS);
    }

    protected final float parseTemperature(byte[] buffer, int index) {
        return parseValue(buffer, index, _SCALE_FACTOR_1_100th);
    }

    @Override
    public T decode(WMBusDevice device) {
        WMBusMessage message = device.getOriginalMessage();
        return decode(device, message.getSecondaryAddress(), message.asBlob());
    }

    // FIXME make this method abstract
    protected abstract T decode(WMBusDevice device, SecondaryAddress address, byte[] buffer);

}
