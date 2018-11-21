package org.openmuc.jmbus;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Representation of a data record (sometimes called variable data block).
 *
 * A data record is the basic data entity of the M-Bus application layer. A variable data structure contains a list of
 * data records. Each data record represents a single data point. A data record consists of three fields: The data
 * information block (DIB), the value information block (VIB) and the data field.
 *
 * The DIB codes the following parameters:
 * <ul>
 * <li>Storage number - a meter can have several storages e.g. to store historical time series data. The storage number
 * 0 signals an actual value.</li>
 * <li>Function - data can have the following four function types: instantaneous value, max value, min value, value
 * during error state.</li>
 * <li>Length and coding of the data field.</li>
 * <li>Tariff - indicates the tariff number of this data field. The data of tariff 0 is usually the sum of all other
 * tariffs.</li>
 * <li>Subunit - can be used by a slave to distinguish several subunits of the metering device</li>
 * </ul>
 *
 * The VIB codes the following parameters:
 * <ul>
 * <li>Description - the meaning of the data value (e.g. "Energy", "Volume" etc.)</li>
 * <li>Unit - the unit of the data value.</li>
 * <li>Multiplier - a factor by which the data value coded in the data field has to be multiplied with.
 * <code>getScaledDataValue()</code> returns the result of the data value multiplied with the multiplier.</li>
 * </ul>
 *
 */
public class DataRecord {

    /**
     * The data value type
     *
     */
    public enum DataValueType {
    LONG,
    DOUBLE,
    DATE,
    STRING,
    BCD,
    NONE;
    }

    /**
     * Function coded in the DIB
     *
     */
    public enum FunctionField {
        /**
         * instantaneous value
         */
        INST_VAL,
        /**
         * maximum value
         */
        MAX_VAL,
        /**
         * minimum value
         */
        MIN_VAL,
        /**
         * value during error state
         */
        ERROR_VAL;
    }

    /**
     * Data description stored in the VIB
     *
     */
    public enum Description {
        ENERGY,
        VOLUME,
        MASS,
        ON_TIME,
        OPERATING_TIME,
        POWER,
        VOLUME_FLOW,
        VOLUME_FLOW_EXT,
        MASS_FLOW,
        FLOW_TEMPERATURE,
        RETURN_TEMPERATURE,
        TEMPERATURE_DIFFERENCE,
        EXTERNAL_TEMPERATURE,
        PRESSURE,
        DATE,
        DATE_TIME,
        VOLTAGE,
        CURRENT,
        AVERAGING_DURATION,
        ACTUALITY_DURATION,
        FABRICATION_NO,
        MODEL_VERSION,
        PARAMETER_SET_ID,
        HARDWARE_VERSION,
        FIRMWARE_VERSION,
        ERROR_FLAGS,
        CUSTOMER,
        RESERVED,
        OPERATING_TIME_BATTERY,
        HCA,
        REACTIVE_ENERGY,
        TEMPERATURE_LIMIT,
        MAX_POWER,
        REACTIVE_POWER,
        REL_HUMIDITY,
        FREQUENCY,
        PHASE,
        EXTENDED_IDENTIFICATION,
        ADDRESS,
        NOT_SUPPORTED,
        MANUFACTURER_SPECIFIC,
        FUTURE_VALUE,
        USER_DEFINED,
        APPARENT_ENERGY,
        CUSTOMER_LOCATION,
        ACCSESS_CODE_OPERATOR,
        ACCSESS_CODE_USER,
        PASSWORD,
        ACCSESS_CODE_SYSTEM_DEVELOPER,
        OTHER_SOFTWARE_VERSION,
        ACCSESS_CODE_SYSTEM_OPERATOR,
        ERROR_MASK,
        SECURITY_KEY,
        DIGITAL_INPUT,
        BAUDRATE,
        DIGITAL_OUTPUT,
        RESPONSE_DELAY_TIME,
        RETRY,
        FIRST_STORAGE_NUMBER_CYCLIC,
        REMOTE_CONTROL,
        LAST_STORAGE_NUMBER_CYCLIC,
        SIZE_STORAGE_BLOCK,
        STORAGE_INTERVALL,
        TARIF_START,
        DURATION_LAST_READOUT,
        TIME_POINT,
        TARIF_DURATION,
        OPERATOR_SPECIFIC_DATA,
        TARIF_PERIOD,
        NUMBER_STOPS,
        LAST_CUMULATION_DURATION,
        SPECIAL_SUPPLIER_INFORMATION,
        PARAMETER_ACTIVATION_STATE,
        CONTROL_SIGNAL,
        WEEK_NUMBER,
        DAY_OF_WEEK,
        REMAINING_BATTERY_LIFE_TIME,
        TIME_POINT_DAY_CHANGE,
        CUMULATION_COUNTER,
        RESET_COUNTER;
    }

    // // Data Information Block that contains a DIF and optionally up to 10 DIFEs
    private byte[] dib;
    // // Value Information Block that contains a VIF and optionally up to 10 VIFEs
    private byte[] vib;

    private Object dataValue;
    private DataValueType dataValueType;

    // DIB fields:
    private FunctionField functionField;

    private long storageNumber; // max is 41 bits
    private int tariff; // max 20 bits
    private short subunit; // max 10 bits

    // VIB fields:
    private Description description;
    private String userDefinedDescription;
    private int multiplierExponent = 0;
    private DlmsUnit unit;

    private boolean dateTypeF = false;
    private boolean dateTypeG = false;

    int dataLength;

    byte[] rawData;

    public byte[] getRawData() {
        return rawData;
    }

    int decode(byte[] buffer, int offset, int length) throws DecodingException {
        int i = offset;

        decodeDib(buffer, i);

        int dataField = buffer[i] & 0x0f;
        dataLength = dataField;
        storageNumber = (buffer[i] & 0x40) >> 6;

        subunit = 0;
        tariff = 0;

        int numDife = 0;
        while ((buffer[i++] & 0x80) == 0x80) {
            subunit += (((buffer[i] & 0x40) >> 6) << numDife);
            tariff += ((buffer[i] & 0x30) >> 4) << (numDife * 2);
            storageNumber += ((buffer[i] & 0x0f) << ((numDife * 4) + 1));
            numDife++;
        }

        multiplierExponent = 0;

        unit = null;

        dib = Arrays.copyOfRange(buffer, offset, i);

        // decode VIB

        int vif = buffer[i++] & 0xff;

        boolean decodeFurtherVifs = false;

        if (vif == 0xfb) {
            decodeAlternateExtendedVif(buffer[i]);
            if ((buffer[i] & 0x80) == 0x80) {
                decodeFurtherVifs = true;
            }
            i++;
        } else if ((vif & 0x7f) == 0x7c) {
            i += decodeUserDefinedVif(buffer, i);
            if ((vif & 0x80) == 0x80) {
                decodeFurtherVifs = true;
            }
        } else if (vif == 0xfd) {
            decodeMainExtendedVif(buffer[i]);
            if ((buffer[i] & 0x80) == 0x80) {
                decodeFurtherVifs = true;
            }
            i++;
        } else {
            decodeMainVif(vif);
            if ((vif & 0x80) == 0x80) {
                decodeFurtherVifs = true;
            }
        }

        if (decodeFurtherVifs) {
            while ((buffer[i++] & 0x80) == 0x80) {
                // TODO these vifes should not be ignored!
            }
        }

        vib = Arrays.copyOfRange(buffer, offset + dib.length, i);

        switch (dataField) {
            case 0x00:
            case 0x08: /* no data - selection for readout request */
                dataValue = null;
                dataValueType = DataValueType.NONE;
                break;
            case 0x01: /* INT8 */
                dataValue = Long.valueOf(buffer[i++]);
                dataValueType = DataValueType.LONG;
                break;
            case 0x02: /* INT16 */
                if (dateTypeG) {
                    int day = (0x1f) & buffer[i];
                    int year1 = ((0xe0) & buffer[i++]) >> 5;
                    int month = (0x0f) & buffer[i];
                    int year2 = ((0xf0) & buffer[i++]) >> 1;
                    int year = (2000 + year1 + year2);

                    Calendar calendar = Calendar.getInstance();

                    calendar.set(year, month - 1, day, 0, 0, 0);

                    dataValue = calendar.getTime();
                    dataValueType = DataValueType.DATE;
                } else {
                    dataValue = Long.valueOf((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8));
                    dataValueType = DataValueType.LONG;
                }
                break;
            case 0x03: /* INT24 */
                if ((buffer[i + 2] & 0x80) == 0x80) {
                    // negative
                    dataValue = Long.valueOf((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8)
                            | ((buffer[i++] & 0xff) << 16) | 0xff << 24);
                } else {
                    dataValue = Long
                            .valueOf((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8) | ((buffer[i++] & 0xff) << 16));
                }
                dataValueType = DataValueType.LONG;
                break;
            case 0x04: /* INT32 */
                if (dateTypeF) {
                    int min = (buffer[i++] & 0x3f);
                    int hour = (buffer[i] & 0x1f);
                    int yearh = (0x60 & buffer[i++]) >> 5;
                    int day = (buffer[i] & 0x1f);
                    int year1 = (0xe0 & buffer[i++]) >> 5;
                    int mon = (buffer[i] & 0x0f);
                    int year2 = (0xf0 & buffer[i++]) >> 1;

                    if (yearh == 0) {
                        yearh = 1;
                    }

                    int year = 1900 + 100 * yearh + year1 + year2;

                    Calendar calendar = Calendar.getInstance();

                    calendar.set(year, mon - 1, day, hour, min, 0);

                    dataValue = calendar.getTime();
                    dataValueType = DataValueType.DATE;
                } else {
                    dataValue = Long.valueOf((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8)
                            | ((buffer[i++] & 0xff) << 16) | ((buffer[i++] & 0xff) << 24));
                    dataValueType = DataValueType.LONG;
                }
                break;
            case 0x05: /* FLOAT32 */
                Float doubleDatavalue = ByteBuffer.wrap(buffer, i, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                i += 4;
                dataValue = Double.valueOf(doubleDatavalue);
                dataValueType = DataValueType.DOUBLE;
                break;
            case 0x06: /* INT48 */
                if ((buffer[i + 5] & 0x80) == 0x80) {
                    // negative
                    dataValue = Long
                            .valueOf((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8) | ((buffer[i++] & 0xff) << 16)
                                    | ((buffer[i++] & 0xff) << 24) | (((long) buffer[i++] & 0xff) << 32)
                                    | (((long) buffer[i++] & 0xff) << 40) | (0xffl << 48) | (0xffl << 56));
                } else {
                    dataValue = Long.valueOf((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8)
                            | ((buffer[i++] & 0xff) << 16) | ((buffer[i++] & 0xff) << 24)
                            | (((long) buffer[i++] & 0xff) << 32) | (((long) buffer[i++] & 0xff) << 40));
                }
                dataValueType = DataValueType.LONG;
                break;
            case 0x07: /* INT64 */
                dataValue = Long.valueOf((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8)
                        | ((buffer[i++] & 0xff) << 16) | ((buffer[i++] & 0xff) << 24)
                        | (((long) buffer[i++] & 0xff) << 32) | (((long) buffer[i++] & 0xff) << 40)
                        | (((long) buffer[i++] & 0xff) << 48) | (((long) buffer[i++] & 0xff) << 56));
                dataValueType = DataValueType.LONG;
                break;
            case 0x09:
                i = setBCD(buffer, i, 1);
                break;
            case 0x0a:
                i = setBCD(buffer, i, 2);
                break;
            case 0x0b:
                i = setBCD(buffer, i, 3);
                break;
            case 0x0c:
                i = setBCD(buffer, i, 4);
                break;
            case 0x0e:
                i = setBCD(buffer, i, 6);
                break;
            case 0x0d:

                int variableLength = buffer[i++] & 0xff;
                int dataLength0x0d;

                if (variableLength < 0xc0) {
                    dataLength0x0d = variableLength;
                } else if ((variableLength >= 0xc0) && (variableLength <= 0xc9)) {
                    dataLength0x0d = 2 * (variableLength - 0xc0);
                } else if ((variableLength >= 0xd0) && (variableLength <= 0xd9)) {
                    dataLength0x0d = 2 * (variableLength - 0xd0);
                } else if ((variableLength >= 0xe0) && (variableLength <= 0xef)) {
                    dataLength0x0d = variableLength - 0xe0;
                } else if (variableLength == 0xf8) {
                    dataLength0x0d = 4;
                } else {
                    throw new DecodingException("Unsupported LVAR Field: " + variableLength);
                }

                // TODO check this:
                // if (variableLength >= 0xc0) {
                // throw new DecodingException("Variable length (LVAR) field >= 0xc0: " + variableLength);
                // }

                byte[] rawData = new byte[dataLength0x0d];

                for (int j = 0; j < dataLength0x0d; j++) {
                    rawData[j] = buffer[i + dataLength0x0d - 1 - j];
                }
                i += dataLength0x0d;

                this.rawData = rawData;
                dataValue = new String(rawData);
                dataValueType = DataValueType.STRING;
                break;
            default:
                String msg = String.format("Unknown Data Field in DIF: %02X.", dataField);
                throw new DecodingException(msg);
        }

        return i;
    }

    private int setBCD(byte[] buffer, int i, int j) {
        dataValue = new Bcd(Arrays.copyOfRange(buffer, i, i + j));
        dataValueType = DataValueType.BCD;
        return i + j;
    }

    private void decodeDib(byte[] buffer, int i) {
        int ff = ((buffer[i] & 0x30) >> 4);
        switch (ff) {
            case 0:
                functionField = FunctionField.INST_VAL;
                break;
            case 1:
                functionField = FunctionField.MAX_VAL;
                break;
            case 2:
                functionField = FunctionField.MIN_VAL;
                break;
            case 3:
                functionField = FunctionField.ERROR_VAL;
                break;
            default:
                this.functionField = null;
        }
    }

    int encode(byte[] buffer, int offset) {

        int i = offset;

        System.arraycopy(dib, 0, buffer, i, dib.length);

        i += dib.length;

        System.arraycopy(vib, 0, buffer, i, vib.length);

        i += vib.length;

        return i - offset;
    }

    /**
     * Returns a byte array containing the DIB (i.e. the DIF and the DIFEs) contained in the data record.
     *
     * @return a byte array containing the DIB
     */
    public byte[] getDib() {
        return dib;
    }

    /**
     * Returns a byte array containing the VIB (i.e. the VIF and the VIFEs) contained in the data record.
     *
     * @return a byte array containing the VIB
     */
    public byte[] getVib() {
        return vib;
    }

    /**
     * Returns the decoded data field of the data record as an Object. The Object is of one of the four types Long,
     * Double, String or Date depending on information coded in the DIB/VIB. The DataType can be checked using
     * getDataValueType().
     *
     * @return the data value
     */
    public Object getDataValue() {
        return dataValue;
    }

    public DataValueType getDataValueType() {
        return dataValueType;
    }

    /**
     * Returns the data (value) multiplied by the multiplier as a Double. If the data is not a number than null is
     * returned.
     *
     * @return the data (value) multiplied by the multiplier as a Double
     */
    public Double getScaledDataValue() {
        try {
            return ((Number) dataValue).doubleValue() * Math.pow(10, multiplierExponent);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public FunctionField getFunctionField() {
        return functionField;
    }

    public long getStorageNumber() {
        return storageNumber;
    }

    public int getTariff() {
        return tariff;
    }

    public short getSubunit() {
        return subunit;
    }

    public Description getDescription() {
        return description;
    }

    public String getUserDefinedDescription() {
        if (description == Description.USER_DEFINED) {
            return userDefinedDescription;
        } else {
            return description.toString();
        }
    }

    /**
     * The multiplier is coded in the VIF. Is always a power of 10. This function returns the exponent. The base is
     * always 10.
     *
     * @return the exponent of the multiplier.
     */
    public int getMultiplierExponent() {
        return multiplierExponent;
    }

    public DlmsUnit getUnit() {
        return unit;
    }

    private void decodeTimeUnit(int vif) {
        if ((vif & 0x02) == 0) {
            if ((vif & 0x01) == 0) {
                unit = DlmsUnit.SECOND;
            } else {
                unit = DlmsUnit.MIN;
            }
        } else {
            if ((vif & 0x01) == 0) {
                unit = DlmsUnit.HOUR;
            } else {
                unit = DlmsUnit.DAY;
            }
        }
    }

    private int decodeUserDefinedVif(byte[] buffer, int offset) throws DecodingException {

        int length = buffer[offset];
        StringBuilder sb = new StringBuilder();
        for (int i = offset + length; i > offset; i--) {
            sb.append((char) buffer[i]);
        }

        description = Description.USER_DEFINED;
        userDefinedDescription = sb.toString();

        return length + 1;

    }

    private void decodeMainVif(int vif) {
        description = Description.NOT_SUPPORTED;

        if ((vif & 0x40) == 0) {
            decodeE0(vif);
        } else {
            decodeE1(vif);

        }

    }

    private void decodeE1(int vif) {
        // E1
        if ((vif & 0x20) == 0) {
            decodeE10(vif);
        } else {
            decodeE11(vif);
        }
    }

    private void decodeE11(int vif) {
        // E11
        if ((vif & 0x10) == 0) {
            decodeE110(vif);
        } else {
            decodeE111(vif);
        }
    }

    private void decodeE111(int vif) {
        // E111
        if ((vif & 0x08) == 0) {
            // E111 0
            if ((vif & 0x04) == 0) {
                description = Description.AVERAGING_DURATION;
            } else {
                description = Description.ACTUALITY_DURATION;
            }
            decodeTimeUnit(vif);
        } else {
            // E111 1
            if ((vif & 0x04) == 0) {
                // E111 10
                if ((vif & 0x02) == 0) {
                    // E111 100
                    if ((vif & 0x01) == 0) {
                        // E111 1000
                        description = Description.FABRICATION_NO;
                    } else {
                        // E111 1001
                        description = Description.EXTENDED_IDENTIFICATION;
                    }
                } else {
                    // E111 101
                    if ((vif & 0x01) == 0) {
                        description = Description.ADDRESS;
                    } else {
                        // E111 1011
                        // Codes used with extension indicator 0xFB (table 29 of DIN EN 13757-3:2011)
                        throw new IllegalArgumentException(
                                "Trying to decode a mainVIF even though it is an alternate extended vif");
                    }
                }
            } else {
                // E111 11
                if ((vif & 0x02) == 0) {
                    // E111 110
                    if ((vif & 0x01) == 0) {
                        // E111 1100
                        // Extension indicator 0xFC: VIF is given in following string
                        description = Description.NOT_SUPPORTED;
                    } else {
                        // E111 1101
                        // Extension indicator 0xFD: main VIFE-code extension table (table 28 of DIN EN
                        // 13757-3:2011)
                        throw new IllegalArgumentException(
                                "Trying to decode a mainVIF even though it is a main extended vif");

                    }
                } else {
                    // E111 111
                    if ((vif & 0x01) == 0) {
                        // E111 1110
                        description = Description.FUTURE_VALUE;
                    } else {
                        // E111 1111
                        description = Description.MANUFACTURER_SPECIFIC;
                    }
                }
            }
        }
    }

    private void decodeE110(int vif) {
        // E110
        if ((vif & 0x08) == 0) {
            // E110 0
            if ((vif & 0x04) == 0) {
                // E110 00
                description = Description.TEMPERATURE_DIFFERENCE;
                multiplierExponent = (vif & 0x03) - 3;
                unit = DlmsUnit.KELVIN;
            } else {
                // E110 01
                description = Description.EXTERNAL_TEMPERATURE;
                multiplierExponent = (vif & 0x03) - 3;
                unit = DlmsUnit.DEGREE_CELSIUS;
            }
        } else {
            // E110 1
            if ((vif & 0x04) == 0) {
                // E110 10
                description = Description.PRESSURE;
                multiplierExponent = (vif & 0x03) - 3;
                unit = DlmsUnit.BAR;
            } else {
                // E110 11
                if ((vif & 0x02) == 0) {
                    // E110 110
                    if ((vif & 0x01) == 0) {
                        // E110 1100
                        description = Description.DATE;
                        dateTypeG = true;
                    } else {
                        // E110 1101
                        description = Description.DATE_TIME;
                        dateTypeF = true;
                    }
                } else {
                    // E110 111
                    if ((vif & 0x01) == 0) {
                        // E110 1110
                        description = Description.HCA;
                        unit = DlmsUnit.RESERVED;
                    } else {
                        description = Description.NOT_SUPPORTED;
                    }

                }

            }
        }
    }

    private void decodeE10(int vif) {
        // E10
        if ((vif & 0x10) == 0) {
            // E100
            if ((vif & 0x08) == 0) {
                // E100 0
                description = Description.VOLUME_FLOW_EXT;
                multiplierExponent = (vif & 0x07) - 7;
                unit = DlmsUnit.CUBIC_METRE_PER_MINUTE;
            } else {
                // E100 1
                description = Description.VOLUME_FLOW_EXT;
                multiplierExponent = (vif & 0x07) - 9;
                unit = DlmsUnit.CUBIC_METRE_PER_SECOND;
            }
        } else {
            // E101
            if ((vif & 0x08) == 0) {
                // E101 0
                description = Description.MASS_FLOW;
                multiplierExponent = (vif & 0x07) - 3;
                unit = DlmsUnit.KILOGRAM_PER_HOUR;
            } else {
                // E101 1
                if ((vif & 0x04) == 0) {
                    // E101 10
                    description = Description.FLOW_TEMPERATURE;
                    multiplierExponent = (vif & 0x03) - 3;
                    unit = DlmsUnit.DEGREE_CELSIUS;
                } else {
                    // E101 11
                    description = Description.RETURN_TEMPERATURE;
                    multiplierExponent = (vif & 0x03) - 3;
                    unit = DlmsUnit.DEGREE_CELSIUS;
                }
            }
        }
    }

    private void decodeE0(int vif) {
        // E0
        if ((vif & 0x20) == 0) {
            decodeE00(vif);
        } else {
            decode01(vif);
        }
    }

    private void decode01(int vif) {
        // E01
        if ((vif & 0x10) == 0) {
            // E010
            if ((vif & 0x08) == 0) {
                // E010 0
                if ((vif & 0x04) == 0) {
                    // E010 00
                    description = Description.ON_TIME;
                } else {
                    // E010 01
                    description = Description.OPERATING_TIME;
                }
                decodeTimeUnit(vif);
            } else {
                // E010 1
                description = Description.POWER;
                multiplierExponent = (vif & 0x07) - 3;
                unit = DlmsUnit.WATT;
            }
        } else {
            // E011
            if ((vif & 0x08) == 0) {
                // E011 0
                description = Description.POWER;
                multiplierExponent = vif & 0x07;
                unit = DlmsUnit.JOULE_PER_HOUR;
            } else {
                // E011 1
                description = Description.VOLUME_FLOW;
                multiplierExponent = (vif & 0x07) - 6;
                unit = DlmsUnit.CUBIC_METRE_PER_HOUR;
            }
        }
    }

    private void decodeE00(int vif) {
        // E00
        if ((vif & 0x10) == 0) {
            // E000
            if ((vif & 0x08) == 0) {
                // E000 0
                description = Description.ENERGY;
                multiplierExponent = (vif & 0x07) - 3;
                unit = DlmsUnit.WATT_HOUR;
            } else {
                // E000 1
                description = Description.ENERGY;
                multiplierExponent = vif & 0x07;
                unit = DlmsUnit.JOULE;
            }
        } else {
            // E001
            if ((vif & 0x08) == 0) {
                // E001 0
                description = Description.VOLUME;
                multiplierExponent = (vif & 0x07) - 6;
                unit = DlmsUnit.CUBIC_METRE;
            } else {
                // E001 1
                description = Description.MASS;
                multiplierExponent = (vif & 0x07) - 3;
                unit = DlmsUnit.KILOGRAM;
            }
        }
    }

    // implements table 28 of DIN EN 13757-3:2013
    private void decodeMainExtendedVif(byte vif) throws DecodingException {
        if ((vif & 0x7f) == 0x0b) { // E000 1011
            description = Description.PARAMETER_SET_ID;
        } else if ((vif & 0x7f) == 0x0c) { // E000 1100
            description = Description.MODEL_VERSION;
        } else if ((vif & 0x7f) == 0x0d) { // E000 1101
            description = Description.HARDWARE_VERSION;
        } else if ((vif & 0x7f) == 0x0e) { // E000 1110
            description = Description.FIRMWARE_VERSION;
        } else if ((vif & 0x7f) == 0x0f) { // E000 1111
            description = Description.OTHER_SOFTWARE_VERSION;
        } else if ((vif & 0x7f) == 0x10) { // E001 0000
            description = Description.CUSTOMER_LOCATION;
        } else if ((vif & 0x7f) == 0x11) { // E001 0001
            description = Description.CUSTOMER;
        } else if ((vif & 0x7f) == 0x12) { // E001 0010
            description = Description.ACCSESS_CODE_USER;
        } else if ((vif & 0x7f) == 0x13) { // E001 0011
            description = Description.ACCSESS_CODE_OPERATOR;
        } else if ((vif & 0x7f) == 0x14) { // E001 0100
            description = Description.ACCSESS_CODE_SYSTEM_OPERATOR;
        } else if ((vif & 0x7f) == 0x15) { // E001 0101
            description = Description.ACCSESS_CODE_SYSTEM_DEVELOPER;
        } else if ((vif & 0x7f) == 0x16) { // E001 0110
            description = Description.PASSWORD;
        } else if ((vif & 0x7f) == 0x17) { // E001 0111
            description = Description.ERROR_FLAGS;
        } else if ((vif & 0x7f) == 0x18) { // E001 1000
            description = Description.ERROR_MASK;
        } else if ((vif & 0x7f) == 0x19) { // E001 1001
            description = Description.SECURITY_KEY;
        } else if ((vif & 0x7f) == 0x1a) { // E001 1010
            description = Description.DIGITAL_OUTPUT;
        } else if ((vif & 0x7f) == 0x1b) { // E001 1011
            description = Description.DIGITAL_INPUT;
        } else if ((vif & 0x7f) == 0x1c) { // E001 1100
            description = Description.BAUDRATE;
        } else if ((vif & 0x7f) == 0x1d) { // E001 1101
            description = Description.RESPONSE_DELAY_TIME;
        } else if ((vif & 0x7f) == 0x1e) { // E001 1110
            description = Description.RETRY;
        } else if ((vif & 0x7f) == 0x1f) { // E001 1111
            description = Description.REMOTE_CONTROL;
        } else if ((vif & 0x7f) == 0x20) { // E010 0000
            description = Description.FIRST_STORAGE_NUMBER_CYCLIC;
        } else if ((vif & 0x7f) == 0x21) { // E010 0001
            description = Description.LAST_STORAGE_NUMBER_CYCLIC;
        } else if ((vif & 0x7f) == 0x22) { // E010 0010
            description = Description.SIZE_STORAGE_BLOCK;
        } else if ((vif & 0x7f) == 0x23) { // E010 0011
            description = Description.RESERVED;
        } else if ((vif & 0x7c) == 0x24) { // E010 01nn
            description = Description.STORAGE_INTERVALL;
            this.unit = unitFor(vif);
        } else if ((vif & 0x7f) == 0x28) { // E010 1000
            description = Description.STORAGE_INTERVALL;
            unit = DlmsUnit.MONTH;
        } else if ((vif & 0x7f) == 0x29) { // E010 1001
            description = Description.STORAGE_INTERVALL;
            unit = DlmsUnit.YEAR;
        } else if ((vif & 0x7f) == 0x2a) { // E010 1010
            description = Description.OPERATOR_SPECIFIC_DATA;
        } else if ((vif & 0x7f) == 0x2b) { // E010 1011
            description = Description.TIME_POINT;
            unit = DlmsUnit.SECOND;
        } else if ((vif & 0x7c) == 0x2c) { // E010 11nn
            description = Description.DURATION_LAST_READOUT;
            this.unit = unitFor(vif);
        } else if ((vif & 0x7c) == 0x30) { // E011 00nn
            description = Description.TARIF_DURATION;
            switch (vif & 0x03) {
                case 0: // E011 0000
                    description = Description.NOT_SUPPORTED; // TODO: TARIF_START (Date/Time)
                    break;
                default:
                    this.unit = unitFor(vif);
            }
        } else if ((vif & 0x7c) == 0x34) { // E011 01nn
            description = Description.TARIF_PERIOD;
            this.unit = unitFor(vif);
        } else if ((vif & 0x7f) == 0x38) { // E011 1000
            description = Description.TARIF_PERIOD;
            unit = DlmsUnit.MONTH;
        } else if ((vif & 0x7f) == 0x39) { // E011 1001
            description = Description.TARIF_PERIOD;
            unit = DlmsUnit.YEAR;
        } else if ((vif & 0x70) == 0x40) { // E100 0000
            description = Description.VOLTAGE;
            multiplierExponent = (vif & 0x0f) - 9;
            unit = DlmsUnit.VOLT;
        } else if ((vif & 0x70) == 0x50) { // E101 0000
            description = Description.CURRENT;
            multiplierExponent = (vif & 0x0f) - 12;
            unit = DlmsUnit.AMPERE;
        } else if ((vif & 0x7f) == 0x60) { // E110 0000
            description = Description.RESET_COUNTER;
        } else if ((vif & 0x7f) == 0x61) { // E110 0001
            description = Description.CUMULATION_COUNTER;
        } else if ((vif & 0x7f) == 0x62) { // E110 0010
            description = Description.CONTROL_SIGNAL;
        } else if ((vif & 0x7f) == 0x63) { // E110 0011
            description = Description.DAY_OF_WEEK; // 1 = Monday; 7 = Sunday; 0 = all Days
        } else if ((vif & 0x7f) == 0x64) { // E110 0100
            description = Description.WEEK_NUMBER;
        } else if ((vif & 0x7f) == 0x65) { // E110 0101
            description = Description.TIME_POINT_DAY_CHANGE;
        } else if ((vif & 0x7f) == 0x66) { // E110 0110
            description = Description.PARAMETER_ACTIVATION_STATE;
        } else if ((vif & 0x7f) == 0x67) { // E110 0111
            description = Description.SPECIAL_SUPPLIER_INFORMATION;
        } else if ((vif & 0x7c) == 0x68) { // E110 10nn
            description = Description.LAST_CUMULATION_DURATION;
            this.unit = unitBiggerFor(vif);
        } else if ((vif & 0x7c) == 0x6c) { // E110 11nn
            description = Description.OPERATING_TIME_BATTERY;
            this.unit = unitBiggerFor(vif);
        } else if ((vif & 0x7f) == 0x70) { // E111 0000
            description = Description.NOT_SUPPORTED; // TODO: BATTERY_CHANGE_DATE_TIME
        } else if ((vif & 0x7f) == 0x71) { // E111 0001
            description = Description.NOT_SUPPORTED; // TODO: RF_LEVEL dBm
        } else if ((vif & 0x7f) == 0x72) { // E111 0010
            description = Description.NOT_SUPPORTED; // TODO: DAYLIGHT_SAVING (begin, ending, deviation)
        } else if ((vif & 0x7f) == 0x73) { // E111 0011
            description = Description.NOT_SUPPORTED; // TODO: Listening window management data type L
        } else if ((vif & 0x7f) == 0x74) { // E111 0100
            description = Description.REMAINING_BATTERY_LIFE_TIME;
            unit = DlmsUnit.DAY;
        } else if ((vif & 0x7f) == 0x75) { // E111 0101
            description = Description.NUMBER_STOPS;
        } else if ((vif & 0x7f) == 0x76) { // E111 0110
            description = Description.MANUFACTURER_SPECIFIC;
        } else if ((vif & 0x7f) >= 0x77) { // E111 0111 - E111 1111
            description = Description.RESERVED;
        } else {
            description = Description.NOT_SUPPORTED;
        }
    }

    private static DlmsUnit unitBiggerFor(byte vif) throws DecodingException {
        int u = vif & 0x03;
        switch (u) {
            case 0: // E110 1100
                return DlmsUnit.HOUR;
            case 1: // E110 1101
                return DlmsUnit.DAY;
            case 2: // E110 1110
                return DlmsUnit.MONTH;
            case 3: // E110 1111
                return DlmsUnit.YEAR;
            default:
                throw new DecodingException(String.format("Unknown unit 0x%02X.", u));
        }
    }

    private static DlmsUnit unitFor(byte vif) throws DecodingException {
        int u = vif & 0x03;
        switch (u) {
            case 0: // E010 1100
                return DlmsUnit.SECOND;
            case 1: // E010 1101
                return DlmsUnit.MIN;
            case 2: // E010 1110
                return DlmsUnit.HOUR;
            case 3: // E010 1111
                return DlmsUnit.DAY;
            default:
                throw new DecodingException(String.format("Unknown unit 0x%02X.", u));
        }
    }

    // implements table 29 of DIN EN 13757-3:2011
    private void decodeAlternateExtendedVif(byte vif) {
        description = Description.NOT_SUPPORTED; // default value

        if ((vif & 0x40) == 0) {
            // E0
            if ((vif & 0x20) == 0) {
                // E00
                if ((vif & 0x10) == 0) {
                    // E000
                    if ((vif & 0x08) == 0) {
                        // E000 0
                        if ((vif & 0x04) == 0) {
                            // E000 00
                            if ((vif & 0x02) == 0) {
                                // E000 000
                                description = Description.ENERGY;
                                multiplierExponent = 5 + (vif & 0x01);
                                unit = DlmsUnit.WATT_HOUR;
                            } else {
                                // E000 001
                                description = Description.REACTIVE_ENERGY;
                                multiplierExponent = 3 + (vif & 0x01);
                                unit = DlmsUnit.VAR_HOUR;
                            }

                        } else {
                            // E000 01
                            if ((vif & 0x02) == 0) {
                                // E000 010
                                description = Description.APPARENT_ENERGY;
                                multiplierExponent = 3 + (vif & 0x01);
                                unit = DlmsUnit.VOLT_AMPERE_HOUR;
                            } else {
                                // E000 011
                                description = Description.NOT_SUPPORTED;
                            }
                        }
                    } else {
                        // E000 1
                        if ((vif & 0x04) == 0) {
                            // E000 10
                            if ((vif & 0x02) == 0) {
                                // E000 100
                                description = Description.ENERGY;
                                multiplierExponent = 8 + (vif & 0x01);
                                unit = DlmsUnit.JOULE;
                            } else {
                                // E000 101
                                description = Description.NOT_SUPPORTED;
                            }

                        } else {
                            // E000 11
                            description = Description.ENERGY;
                            multiplierExponent = 5 + (vif & 0x03);
                            unit = DlmsUnit.CALORIFIC_VALUE;
                        }
                    }
                } else {
                    // E001
                    if ((vif & 0x08) == 0) {
                        // E001 0
                        if ((vif & 0x04) == 0) {
                            // E001 00
                            if ((vif & 0x02) == 0) {
                                // E001 000
                                description = Description.VOLUME;
                                multiplierExponent = 2 + (vif & 0x01);
                                unit = DlmsUnit.CUBIC_METRE;
                            } else {
                                // E001 001
                                description = Description.NOT_SUPPORTED;
                            }
                        } else {
                            // E001 01
                            description = Description.REACTIVE_POWER;
                            multiplierExponent = (vif & 0x03);
                            unit = DlmsUnit.VAR;
                        }
                    } else {
                        // E001 1
                        if ((vif & 0x04) == 0) {
                            // E001 10
                            if ((vif & 0x02) == 0) {
                                // E001 100
                                description = Description.MASS;
                                multiplierExponent = 5 + (vif & 0x01);
                                unit = DlmsUnit.KILOGRAM;
                            } else {
                                // E001 101
                                description = Description.REL_HUMIDITY;
                                multiplierExponent = -1 + (vif & 0x01);
                                unit = DlmsUnit.PERCENTAGE;
                            }

                        } else {
                            // E001 11
                            description = Description.NOT_SUPPORTED;
                        }
                    }

                }
            } else {
                // E01
                if ((vif & 0x10) == 0) {
                    // E010
                    if ((vif & 0x08) == 0) {
                        // E010 0
                        if ((vif & 0x04) == 0) {
                            // E010 00
                            if ((vif & 0x02) == 0) {
                                // E010 000
                                if ((vif & 0x01) == 0) {
                                    // E010 0000
                                    description = Description.VOLUME;
                                    multiplierExponent = 0;
                                    unit = DlmsUnit.CUBIC_FEET;
                                } else {
                                    // E010 0001
                                    description = Description.VOLUME;
                                    multiplierExponent = -1;
                                    unit = DlmsUnit.CUBIC_FEET;
                                }
                            } else {
                                // E010 001
                                // outdated value !
                                description = Description.VOLUME;
                                multiplierExponent = -1 + (vif & 0x01);
                                unit = DlmsUnit.US_GALLON;
                            }
                        } else {
                            // E010 01
                            if ((vif & 0x02) == 0) {
                                // E010 010
                                if ((vif & 0x01) == 0) {
                                    // E010 0100
                                    // outdated value !
                                    description = Description.VOLUME_FLOW;
                                    multiplierExponent = -3;
                                    unit = DlmsUnit.US_GALLON_PER_MINUTE;
                                } else {
                                    // E010 0101
                                    // outdated value !
                                    description = Description.VOLUME_FLOW;
                                    multiplierExponent = 0;
                                    unit = DlmsUnit.US_GALLON_PER_MINUTE;
                                }
                            } else {
                                // E010 011
                                if ((vif & 0x01) == 0) {
                                    // E010 0110
                                    // outdated value !
                                    description = Description.VOLUME_FLOW;
                                    multiplierExponent = 0;
                                    unit = DlmsUnit.US_GALLON_PER_HOUR;
                                } else {
                                    // E010 0111
                                    description = Description.NOT_SUPPORTED;
                                }
                            }

                        }
                    } else {
                        // E010 1
                        if ((vif & 0x04) == 0) {
                            // E010 10
                            if ((vif & 0x02) == 0) {
                                // E010 100
                                description = Description.POWER;
                                multiplierExponent = 5 + (vif & 0x01);
                                unit = DlmsUnit.WATT;
                            } else {
                                if ((vif & 0x01) == 0) {
                                    // E010 1010
                                    description = Description.PHASE;
                                    multiplierExponent = -1; // is -1 or 0 correct ??
                                    unit = DlmsUnit.DEGREE;
                                }
                                // TODO same
                                // else {
                                // // E010 1011
                                // description = Description.PHASE;
                                // multiplierExponent = -1; // is -1 or 0 correct ??
                                // unit = DlmsUnit.DEGREE;
                                // }
                            }
                        } else {
                            // E010 11
                            description = Description.FREQUENCY;
                            multiplierExponent = -3 + (vif & 0x03);
                            unit = DlmsUnit.HERTZ;
                        }
                    }
                } else {
                    // E011
                    if ((vif & 0x08) == 0) {
                        // E011 0
                        if ((vif & 0x04) == 0) {
                            // E011 00
                            if ((vif & 0x02) == 0) {
                                // E011 000
                                description = Description.POWER;
                                multiplierExponent = 8 + (vif & 0x01);
                                unit = DlmsUnit.JOULE_PER_HOUR;
                            } else {
                                // E011 001
                                description = Description.NOT_SUPPORTED;
                            }
                        } else {
                            // E011 01
                            description = Description.APPARENT_ENERGY;
                            multiplierExponent = (vif & 0x03);
                            unit = DlmsUnit.VOLT_AMPERE;
                        }
                    } else {
                        // E011 1
                        description = Description.NOT_SUPPORTED;
                    }
                }
            }
        } else {
            // E1
            if ((vif & 0x20) == 0) {
                // E10
                if ((vif & 0x10) == 0) {
                    // E100
                    description = Description.NOT_SUPPORTED;
                } else {
                    // E101
                    if ((vif & 0x08) == 0) {
                        // E101 0
                        description = Description.NOT_SUPPORTED;
                    } else {
                        // E101 1
                        if ((vif & 0x04) == 0) {
                            // E101 10
                            // outdated value !
                            description = Description.FLOW_TEMPERATURE;
                            multiplierExponent = (vif & 0x03) - 3;
                            unit = DlmsUnit.DEGREE_FAHRENHEIT;
                        } else {
                            // E101 11
                            // outdated value !
                            description = Description.RETURN_TEMPERATURE;
                            multiplierExponent = (vif & 0x03) - 3;
                            unit = DlmsUnit.DEGREE_FAHRENHEIT;
                        }
                    }
                }
            } else {
                // E11
                if ((vif & 0x10) == 0) {
                    // E110
                    if ((vif & 0x08) == 0) {
                        // E110 0
                        if ((vif & 0x04) == 0) {
                            // E110 00
                            // outdated value !
                            description = Description.TEMPERATURE_DIFFERENCE;
                            multiplierExponent = (vif & 0x03) - 3;
                            unit = DlmsUnit.DEGREE_FAHRENHEIT;
                        } else {
                            // E110 01
                            // outdated value !
                            description = Description.FLOW_TEMPERATURE;
                            multiplierExponent = (vif & 0x03) - 3;
                            unit = DlmsUnit.DEGREE_FAHRENHEIT;
                        }
                    } else {
                        // E110 1
                        description = Description.NOT_SUPPORTED;
                    }
                } else {
                    // E111
                    if ((vif & 0x08) == 0) {
                        // E111 0
                        if ((vif & 0x04) == 0) {
                            // E111 00
                            // outdated value !
                            description = Description.TEMPERATURE_LIMIT;
                            multiplierExponent = (vif & 0x03) - 3;
                            unit = DlmsUnit.DEGREE_FAHRENHEIT;
                        } else {
                            // E111 01
                            description = Description.TEMPERATURE_LIMIT;
                            multiplierExponent = (vif & 0x03) - 3;
                            unit = DlmsUnit.DEGREE_CELSIUS;
                        }
                    } else {
                        // E111 1
                        description = Description.MAX_POWER;
                        multiplierExponent = (vif & 0x07) - 3;
                        unit = DlmsUnit.WATT;
                    }
                }
            }

        }

    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder().append("DIB:").append(printHexBinary(dib)).append(", VIB:")
                .append(printHexBinary(vib)).append(" -> descr:").append(description);

        if (description == Description.USER_DEFINED) {
            builder.append(" :").append(getUserDefinedDescription());
        }
        builder.append(", function:").append(functionField);

        if (storageNumber > 0) {
            builder.append(", storage:").append(storageNumber);
        }

        if (tariff > 0) {
            builder.append(", tariff:").append(tariff);
        }

        if (subunit > 0) {
            builder.append(", subunit:").append(subunit);
        }

        final String valuePlacHolder = ", value:";
        final String scaledValueString = ", scaled value:";

        switch (dataValueType) {
            case DATE:
            case STRING:
                builder.append(valuePlacHolder).append((dataValue).toString());
                break;
            case DOUBLE:
                builder.append(scaledValueString).append(getScaledDataValue());
                break;
            case LONG:
                if (multiplierExponent == 0) {
                    builder.append(valuePlacHolder).append(dataValue);
                } else {
                    builder.append(scaledValueString).append(getScaledDataValue());
                }
                break;
            case BCD:
                if (multiplierExponent == 0) {
                    builder.append(valuePlacHolder).append((dataValue).toString());
                } else {
                    builder.append(scaledValueString).append(getScaledDataValue());
                }
                break;
            case NONE:
                builder.append(", value:NONE");
                break;
        }

        if (unit != null) {
            builder.append(", unit:").append(unit);
            if (!unit.getUnit().isEmpty()) {
                builder.append(", ").append(unit.getUnit());
            }
        }

        return builder.toString();

    }

    public int getDataLength() {
        return dataLength;
    }

}
