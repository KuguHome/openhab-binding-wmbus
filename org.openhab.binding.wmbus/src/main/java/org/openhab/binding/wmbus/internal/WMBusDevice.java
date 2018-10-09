/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.internal;

import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.WMBusMessage;

/**
 * The {@link WMBusDevice} class defines WMBusDevice
 *
 * @author Hanno - Felix Wagner - Initial contribution
 */

public class WMBusDevice {

    private WMBusMessage originalMessage;

    public WMBusDevice(WMBusMessage originalMessage) {
        this.originalMessage = originalMessage;
    }

    public WMBusMessage getOriginalMessage() {
        return originalMessage;
    }

    public void decode() throws DecodingException {
        originalMessage.getVariableDataResponse().decode();
    }

    public String getDeviceId() {
        return originalMessage.getSecondaryAddress().getDeviceId().toString();
    }

    public DataRecord findRecord(RecordType recordType) {
        for (DataRecord record : getOriginalMessage().getVariableDataResponse().getDataRecords()) {
            if (recordType.matches(record)) {
                return record;
            }
        }
        return null;
    }

    public DataRecord findRecord(byte[] dib, byte[] vib) {
        return findRecord(new RecordType(dib, vib));
    }
}
