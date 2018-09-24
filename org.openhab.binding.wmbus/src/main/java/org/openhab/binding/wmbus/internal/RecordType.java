/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.internal;

import java.util.Arrays;

import org.openmuc.jmbus.DataRecord;

/**
 * The {@link RecordType} class defines RecordType
 *
 * @author Hanno - Felix Wagner - Initial contribution
 */

public class RecordType {

    private final byte[] dib;
    private final byte[] vib;

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

}
