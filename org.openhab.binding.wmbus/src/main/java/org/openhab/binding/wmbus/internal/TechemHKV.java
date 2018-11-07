/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.internal;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.VariableDataStructure;
import org.openmuc.jmbus.wireless.WMBusMessage;

/**
 * The {@link TechemHKV} class Represents a Message of a Techem Heizkostenverteiler (heat cost allocator)
 *
 * @author Hanno - Felix Wagner - Roman Malyugin - Initial contribution
 */

public class TechemHKV extends WMBusDevice {

    public TechemHKV(WMBusMessage originalMessage, WMBusAdapter adapter) {
        super(originalMessage, adapter);
    }

    int ciField;
    String status = "";
    ZonedDateTime lastDate = null;
    ZonedDateTime curDate = null;
    int lastVal = -1;
    int curVal = -1;
    float t1 = -1;
    float t2 = -1;
    byte[] historyBytes = new byte[27];
    String history = "";

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    VariableDataStructure vdr;

    @Override
    public void decode() throws DecodingException {
        byte[] hkvBuffer = getOriginalMessage().asBlob();
        SecondaryAddress secondaryAddress = getOriginalMessage().getSecondaryAddress();
        vdr = getOriginalMessage().getVariableDataResponse();

        int offset = 10;
        if (hkvBuffer.length < 11) {
            throw new DecodingException("No known Techem HKV message. hkvBuffer length = " + hkvBuffer.length);
        } else {
            ciField = hkvBuffer[offset + 0] & 0xff;
            int historyLength = hkvBuffer.length - 24;

            if (((ciField == 0xa0 || ciField == 0xa2) && secondaryAddress.getManufacturerId().equals("TCH"))
                    && historyLength > 0) {
                byte[] temp = { hkvBuffer[offset + 1] };
                status = HexConverter.bytesToHex(temp);
                lastDate = parseLastDate(offset + 2);
                curDate = parseCurrentDate(offset + 6);
                lastVal = parseBigEndianInt(offset + 4);
                curVal = parseBigEndianInt(offset + 8);
                t1 = parseTemp(offset + 10);
                t2 = parseTemp(offset + 12);

                historyBytes = new byte[historyLength];

                System.arraycopy(hkvBuffer, 24, historyBytes, 0, historyLength);
                history = HexConverter.bytesToHex(historyBytes);
            } else {
                throw new DecodingException("No known Techem HKV message. ciField=" + ciField + ", Manufacturer="
                        + secondaryAddress.getManufacturerId());
            }
        }
    }

    public ZonedDateTime getLastDate() {
        return lastDate;
    }

    public ZonedDateTime getCurDate() {
        return curDate;
    }

    public int getLastVal() {
        return lastVal;
    }

    public int getCurVal() {
        return curVal;
    }

    public float getT1() {
        return t1;
    }

    public float getT2() {
        return t2;
    }

    public String getHistory() {
        return history;
    }

    int parseBigEndianInt(int i) {
        if (getOriginalMessage().asBlob().length <= i + 1) {
            return 0;
        }
        return (getOriginalMessage().asBlob()[i] & 0xFF) + ((getOriginalMessage().asBlob()[i + 1] & 0xFF) << 8);
    }

    float parseTemp(int i) {
        float tempint = parseBigEndianInt(i);

        return tempint / 100;
        // return String.format("%.2f", tempint / 100)+"°C";
    }

    private ZonedDateTime parseLastDate(int i) {
        int dateint = parseBigEndianInt(i);

        int day = (dateint >> 0) & 0x1F;
        int month = (dateint >> 5) & 0x0F;
        int year = (dateint >> 9) & 0x3F;

        ZonedDateTime zonedDateTime = ZonedDateTime.of(2000 + year, month, day, 0, 0, 0, 0, ZoneId.systemDefault());

        return zonedDateTime.truncatedTo(ChronoUnit.DAYS);
    }

    private ZonedDateTime parseCurrentDate(int i) {
        int dateint = parseBigEndianInt(i);

        int day = (dateint >> 4) & 0x1F;
        int month = (dateint >> 9) & 0x0F;
        // int year = (dateint >> 13) & 0x07;

        if (day <= 0) {
            day = 1;
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.now().withMonth(month).withDayOfMonth(day);

        return zonedDateTime.truncatedTo(ChronoUnit.SECONDS);
    }

    public String renderTechemFields() {
        String s = "";

        s += "Last Date: " + dateFormatter.format(lastDate);
        s += ", Last Value: " + lastVal;

        s += ", Current Date: " + dateFormatter.format(curDate);
        s += ", Current Value: " + curVal;

        s += ", T1: " + String.format("%.2f", t1) + "°C";
        s += ", T2: " + String.format("%.2f", t2) + "°C";

        s += ", History: " + history;
        return s;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        if (getOriginalMessage().getVariableDataResponse() == null) {
            builder.append("TechemHKV: Message has not been decoded. Bytes of this message: ");
            // HexConverter.appendHexString(builder, originalMessage.asBlob(), 0, originalMessage.asBlob().length);
            return builder.toString();
        } else {
            builder.append(new Date()).append(";").append(getOriginalMessage().getRssi()).append(";")
                    .append(getOriginalMessage().getControlField()).append(";")
                    .append(getOriginalMessage().getSecondaryAddress().getManufacturerId()).append(";")
                    .append(getOriginalMessage().getSecondaryAddress().getDeviceId()).append(";")
                    .append(getOriginalMessage().getSecondaryAddress().getVersion()).append(";")
                    .append(getOriginalMessage().getSecondaryAddress().getDeviceType()).append(";").append(ciField)
                    .append(";").append(status).append(";").append(dateFormatter.format(lastDate)).append(";")
                    .append(lastVal).append(";").append(dateFormatter.format(curDate)).append(";").append(curVal)
                    .append(";").append(t1).append(";").append(t2).append(";").append(history).append(";")
                    .append(HexConverter.bytesToHex(getOriginalMessage().asBlob()));
            return builder.toString();
        }
    }

    public Integer getRssi() {
        return getOriginalMessage().getRssi();
    }

}
