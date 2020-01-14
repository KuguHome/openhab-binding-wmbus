/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very basic wrapper around WMBus Message which turns it into readable sequence of bytes allowing to navigate over
 * data part with automatic adjustment of reader index.
 *
 * @author Łukasz Dywicki - initial contribution.
 */
public class Buffer {

    private final Logger logger = LoggerFactory.getLogger(Buffer.class);
    private final ByteBuffer buffer;

    public final static Function<Float, Float> _SCALE_FACTOR_1_10th = value -> value / 10;
    public final static Function<Float, Float> _SCALE_FACTOR_1_100th = value -> value / 100;

    public Buffer(WMBusMessage message) {
        this(message.asBlob(), message.getSecondaryAddress());
    }

    public Buffer(WMBusMessage message, SecondaryAddress secondaryAddress) {
        this(message.asBlob(), secondaryAddress);
    }

    public Buffer(byte[] buffer, SecondaryAddress secondaryAddress) {
        this(buffer, secondaryAddress.asByteArray().length);
    }

    public Buffer(byte[] buffer) {
        this(buffer, 0);
    }

    public Buffer(byte[] buffer, int offset) {
        this.buffer = ByteBuffer.wrap(buffer);
        skip(offset);
    }

    public Buffer skip(int bytes) {
        int position = buffer.position() + bytes;
        if (position > buffer.limit()) {
            throw new BufferOverflowException();
        }

        this.buffer.position(position);
        return this;
    }

    public byte readByte() {
        return buffer.get();
    }

    public int readInt() {
        int number = orderedRead(ByteOrder.LITTLE_ENDIAN, ByteBuffer::getInt);
        return number >> 8;
    }

    public short readShort() {
        short number = orderedRead(ByteOrder.LITTLE_ENDIAN, ByteBuffer::getShort);
        return number;
    }

    public float readFloat() {
        float number = readShort();
        return number;
    }

    public LocalDateTime readPastDate() {
        int dateint = readShort();

        int day = dateint & 0x1F;
        int month = (dateint >> 5) & 0x0F;
        int year = (dateint >> 9) & 0x3F;

        return LocalDate.of(2000 + year, month, day).atStartOfDay();
    }

    public LocalDateTime readCurrentDate() {
        int position = buffer.position();
        int dateint = readShort();

        int day = (dateint >> 4) & 0x1F;
        int month = (dateint >> 9) & 0x0F;

        if (day <= 0) {
            logger.trace("Detected invalid day number {} in byte representation: {}, changing to 1st day of month", day,
                    HexUtils.bytesToHex(new byte[] {buffer.get(position)}));
            day = 1;
        }
        if (month <= 0) {
            logger.trace("Detected invalid month number {} in byte representation: {}, changing to last month of year",
                    month, HexUtils.bytesToHex(new byte[] {buffer.get(position)}));
            month = 12;
        }

        LocalDateTime dateTime = LocalDateTime.now().withMonth(month).withDayOfMonth(day);
        return dateTime.truncatedTo(ChronoUnit.SECONDS);
    }

    public float readFloat(Function<Float, Float> scale) {
        Float apply = scale.apply(readFloat());
        return apply;
    }

    public int position() {
        return buffer.position();
    }

    private final <T> T orderedRead(ByteOrder readOrder, Function<ByteBuffer, T> callback) {
        ByteOrder byteOrder = buffer.order();
        if (!readOrder.equals(byteOrder)) {
            try {
                buffer.order(readOrder);
                return callback.apply(buffer);
            } finally {
                buffer.order(byteOrder);
            }
        }

        return callback.apply(buffer);
    }

}
