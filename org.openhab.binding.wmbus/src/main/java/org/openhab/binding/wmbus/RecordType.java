/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus;

import java.util.Arrays;

import org.eclipse.smarthome.core.util.HexUtils;
import org.openmuc.jmbus.DataRecord;

/**
 * The {@link RecordType} class defines RecordType
 *
 * @author Hanno - Felix Wagner - Initial contribution
 * @author Łukasz Dywicki - Hash/equality calculation and toString implementation.
 */

public class RecordType {

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

    boolean matches(DataRecord record) {
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
