package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.VariableDataStructure;
import org.openmuc.jmbus.wireless.WMBusMessage;

/**
 *
 * Represents a Message of a Techem Heizkostenverteiler (heat cost allocator)
 *
 */
public class TechemHKVMessage {

    int ciField;
    String status = "";
    Calendar lastDate = null;
    Calendar curDate = null;
    int lastVal = -1;
    int curVal = -1;
    float t1 = -1;
    float t2 = -1;
    byte[] historyBytes = new byte[27];
    String history = "";

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    VariableDataStructure vdr;

    private WMBusMessage originalMessage;

    public TechemHKVMessage(WMBusMessage originalMessage) {
        this.originalMessage = originalMessage;
    }

    public void decodeDeep() throws DecodingException {
        byte[] hkvBuffer = originalMessage.asBlob();
        SecondaryAddress secondaryAddress = originalMessage.getSecondaryAddress();
        vdr = originalMessage.getVariableDataResponse();

        int offset = 10;

        ciField = hkvBuffer[offset + 0] & 0xff;

        if ((ciField == 0xa0 || ciField == 0xa2) && secondaryAddress.getManufacturerId().equals("TCH")) {
            byte[] temp = { hkvBuffer[offset + 1] }; // TODO Methode für byte -> String hinzufügen
            status = HexConverter.bytesToHex(temp);
            lastDate = parseLastDate(offset + 2);
            curDate = parseCurrentDate(offset + 6);
            lastVal = parseBigEndianInt(offset + 4);
            curVal = parseBigEndianInt(offset + 8);
            t1 = parseTemp(offset + 10);
            t2 = parseTemp(offset + 12);

            System.arraycopy(hkvBuffer, 24, historyBytes, 0, hkvBuffer.length - 24);
            history = HexConverter.bytesToHex(historyBytes);

        } else {
            throw new DecodingException("No known Techem HKV message");
        }
    }

    public Calendar getLastDate() {
        return lastDate;
    }

    public Calendar getCurDate() {
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
        return (originalMessage.asBlob()[i] & 0xFF) + ((originalMessage.asBlob()[i + 1] & 0xFF) << 8);
    }

    float parseTemp(int i) {
        float tempint = parseBigEndianInt(i);

        return tempint / 100;
        // return String.format("%.2f", tempint / 100)+"°C";
    }

    private Calendar parseLastDate(int i) {
        int dateint = parseBigEndianInt(i);

        int day = (dateint >> 0) & 0x1F;
        int month = (dateint >> 5) & 0x0F;
        int year = (dateint >> 9) & 0x3F;

        // return LocalDate.of(2000+year, month, day);
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2000 + year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        return calendar;
    }

    private Calendar parseCurrentDate(int i) {
        int dateint = parseBigEndianInt(i);

        int day = (dateint >> 4) & 0x1F;
        int month = (dateint >> 9) & 0x0F;
        // int year = (dateint >> 13) & 0x07;
        Calendar calendar = new GregorianCalendar();
        calendar.set(calendar.get(Calendar.YEAR), month - 1, day);
        return calendar;
        // return LocalDate.of( LocalDate.now().getYear(), month, day);
    }

    public String renderTechemFields() {
        String s = "";

        s += "Last Date: " + dateFormat.format(lastDate.getTime());
        s += ", Last Value: " + lastVal;

        s += ", Current Date: " + dateFormat.format(curDate.getTime());
        s += ", Current Value: " + curVal;

        s += ", T1: " + String.format("%.2f", t1) + "°C";
        s += ", T2: " + String.format("%.2f", t2) + "°C";

        s += ", History: " + history;
        return s;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        if (originalMessage.getVariableDataResponse() == null) {
            builder.append("Message has not been decoded. Bytes of this message: ");
//            HexConverter.appendHexString(builder, originalMessage.asBlob(), 0, originalMessage.asBlob().length);
            return builder.toString();
        } else {
            builder.append(new Date()).append(";").append(originalMessage.getRssi()).append(";").append(originalMessage.getControlField()).append(";")
                    .append(originalMessage.getSecondaryAddress().getManufacturerId()).append(";")
                    .append(originalMessage.getSecondaryAddress().getDeviceId()).append(";").append(originalMessage.getSecondaryAddress().getVersion())
                    .append(";").append(originalMessage.getSecondaryAddress().getDeviceType()).append(";").append(ciField).append(";")
                    .append(status).append(";").append(dateFormat.format(lastDate.getTime())).append(";")
                    .append(lastVal).append(";").append(dateFormat.format(curDate.getTime())).append(";").append(curVal)
                    .append(";").append(t1).append(";").append(t2).append(";").append(history).append(";")
                    .append(HexConverter.bytesToHex(originalMessage.asBlob()));
            return builder.toString();
        }
    }

    public WMBusMessage getOriginalMessage() {
        return originalMessage;
    }

}