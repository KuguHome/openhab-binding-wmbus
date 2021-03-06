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

package org.openhab.binding.wmbus;

import java.util.Arrays;

import org.openhab.core.util.HexUtils;
import org.openmuc.jmbus.DataRecord;

/**
 * The {@link RecordType} class defines RecordType
 *
 * @author Hanno - Felix Wagner - Initial contribution
 * @author Łukasz Dywicki - Hash/equality calculation and toString implementation.
 */

public class RecordType {

    /**
     * Constant for manufacturer data.
     *
     * Below combination of dib/vib is intentionally invalid. It is a mark for manufacturer specific data which can be
     * appended as part of standard payload in the frame.
     */
    public final static RecordType MANUFACTURER_DATA = new RecordType(new byte[] { 0x0, 0x0, 0x0, 0x0 },
            new byte[] { 0x0, 0x0, 0x0, 0x0 });

    private final byte[] dib;
    private final byte[] vib;

    public RecordType(byte[] dib, int vib) {
        this(dib, new byte[] { (byte) vib });
    }

    public RecordType(int dib, byte[] vib) {
        this(new byte[] { (byte) dib }, vib);
    }

    public RecordType(byte[] dib, byte[] vib) {
        this.dib = dib;
        this.vib = vib;
    }

    public RecordType(int dib, int vib) {
        this(new byte[] { (byte) dib }, new byte[] { (byte) vib });
    }

    public byte[] getDib() {
        return dib;
    }

    public byte[] getVib() {
        return vib;
    }

    public boolean matches(DataRecord record) {
        return Arrays.equals(record.getDib(), getDib()) && Arrays.equals(record.getVib(), getVib());
    }

    @Override
    public String toString() {
        return "RecordType DIB:" + HexUtils.bytesToHex(dib) + ", VIB:" + HexUtils.bytesToHex(vib);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(dib);
        result = prime * result + Arrays.hashCode(vib);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RecordType other = (RecordType) obj;
        if (!Arrays.equals(dib, other.dib)) {
            return false;
        }
        if (!Arrays.equals(vib, other.vib)) {
            return false;
        }
        return true;
    }
}
