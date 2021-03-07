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
package org.openhab.binding.wmbus.device.itron;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import org.openhab.binding.wmbus.device.techem.decoder.Buffer;

public class ItronManufacturerDataParser {

    private final static byte[] EMPTY_SHORT_DATE_1 = new byte[] { 0x0, 0x0, 0x1, 0x1 };
    private final static byte[] EMPTY_SHORT_DATE_2 = new byte[] { 0x0, 0x0, 0x0, 0x0 };
    private final Buffer buffer;

    ItronManufacturerDataParser(Buffer buffer) {
        this.buffer = buffer;
    }

    public LocalDateTime readShortDateTime() {
        return parseShortDateTime(buffer.readBytes(4));
    }

    public LocalDateTime readLongDateTime() {
        return parseLongDateTime(buffer.readBytes(5));
    }

    private LocalDateTime parseShortDateTime(byte[] date) {
        if (Arrays.equals(date, EMPTY_SHORT_DATE_1) || Arrays.equals(date, EMPTY_SHORT_DATE_2)) {
            return null;
        }

        int minute = date[0] & 0xFF >> 2;

        int yearLSB = (date[2] & 0xFF) >> 5;
        int yearMSB = ((date[3] & 0xFF) & 0xF0) >> 1;
        int hour = (date[1] & 0xFF >> 3);
        int day = (date[2] & 0xFF >> 3);
        int month = (date[3] & 0xFF >> 4);

        // ((0x2C & 0xF0) >> 0x1) | (0x7D >> 0x5) = 0x13 = 19d
        int year = yearMSB | yearLSB;

        return LocalDateTime.of(2000 + year, month, day, hour, minute);
    }

    private LocalDateTime parseLongDateTime(byte[] date) {
        int second = (date[0] & 0xFF) >> 2;
        int minute = (date[1] & 0xFF) >> 2;
        int hour = (date[2] & 0xFF >> 3);
        int day = (date[2] & 0xFF >> 4);
        int month = (date[3] & 0xFF >> 4);

        int yearLSB = (date[3] & 0xFF) >> 5;
        int yearMSB = ((date[4] & 0xFF) & 0xF0) >> 1;

        // ((0x2C & 0xF0) >> 0x1) | (0x7D >> 0x5) = 0x13 = 19d
        int year = yearMSB | yearLSB;

        LocalTime time = LocalTime.of(hour, minute, second);
        DayOfWeek dayOfWeek = DayOfWeek.of(day);
        LocalDate today = LocalDate.now();
        LocalDate parsed = LocalDate.of(year, month, today.getDayOfMonth());

        LocalTime timeWithDay = time.with(dayOfWeek);
        return parsed.atStartOfDay().with(timeWithDay);
    }
}
