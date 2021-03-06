/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

/**
 * This class represents a binary-coded decimal (BCD) number as defined by the M-Bus standard. The class provides
 * methods to convert the BCD to other types such as <code>double</code>, <code>int</code> or <code>String</code>.
 */
public class Bcd extends Number {

    private static final long serialVersionUID = 790515601507532939L;
    private final byte[] value;

    /**
     * Constructs a <code>Bcd</code> from the given bytes. The constructed Bcd will use the given byte array for
     * internal storage of its value. It is therefore recommended not to change the byte array after construction.
     * 
     * @param bcdBytes
     *            the byte array to be used for construction of the <code>Bcd</code>.
     */
    public Bcd(byte[] bcdBytes) {
        this.value = bcdBytes;
    }

    public byte[] getBytes() {
        return value;
    }

    @Override
    public String toString() {
        byte[] bytes = new byte[value.length * 2];
        int c = 0;

        if ((value[value.length - 1] & 0xf0) == 0xf0) {
            bytes[c++] = 0x2d;
        } else {
            bytes[c++] = (byte) (((value[value.length - 1] >> 4) & 0x0f) + 48);
        }

        bytes[c++] = (byte) ((value[value.length - 1] & 0x0f) + 48);

        for (int i = value.length - 2; i >= 0; i--) {
            bytes[c++] = (byte) (((value[i] >> 4) & 0x0f) + 48);
            bytes[c++] = (byte) ((value[i] & 0x0f) + 48);
        }

        return new String(bytes);
    }

    @Override
    public double doubleValue() {
        return longValue();
    }

    @Override
    public float floatValue() {
        return longValue();
    }

    @Override
    public int intValue() {
        return (int) longValue();
    }

    @Override
    public long longValue() {
        long result = 0l;
        long factor = 1l;

        for (int i = 0; i < (value.length - 1); i++) {
            result += (value[i] & 0x0f) * factor;
            factor = factor * 10l;
            result += ((value[i] >> 4) & 0x0f) * factor;
            factor = factor * 10l;
        }

        result += (value[value.length - 1] & 0x0f) * factor;
        factor = factor * 10l;

        if ((value[value.length - 1] & 0xf0) == 0xf0) {
            result = result * -1;
        } else {
            result += ((value[value.length - 1] >> 4) & 0x0f) * factor;
        }

        return result;
    }
}
