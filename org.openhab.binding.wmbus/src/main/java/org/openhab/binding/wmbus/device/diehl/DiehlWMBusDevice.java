/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.diehl;

import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openmuc.jmbus.Bcd;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.WMBusMessage;

/**
 * Representation of Diehl devices.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class DiehlWMBusDevice extends WMBusDevice {

    public DiehlWMBusDevice(WMBusDevice device) throws DecodingException {
        this(device.getOriginalMessage(), device.getAdapter());
    }

    public DiehlWMBusDevice(WMBusMessage originalMessage, WMBusAdapter adapter) throws DecodingException {
        super(originalMessage, adapter);
    }

    @Override
    public DataRecord findRecord(RecordType recordType) {
        final DataRecord record = super.findRecord(recordType);

        // FIXME this is dirty hack and assumption which might be very wrong
        if (DiehlBindingConstants.HCA_STORAGE_1_4D.equals(recordType)) {
            // HCD field 4D is read as String, lets convert it to Bcd and then Long.
            return new DataRecord() {
                @Override
                public DataValueType getDataValueType() {
                    return DataValueType.LONG;
                }

                @Override
                public Long getDataValue() {
                    byte[] bytes = clean(record.getRawData());
                    return new Bcd(bytes).longValue();
                }

                @Override
                public Double getScaledDataValue() {
                    return getDataValue().doubleValue();
                }

                private byte[] clean(byte[] rawData) {
                    byte[] data = new byte[rawData.length];
                    int offset = 0;
                    for (int index = 0; index < rawData.length; index++) {
                        if (offset == 0) {
                            if (rawData[index] == 0x00) {
                                continue;
                            }
                        }
                        data[offset++] = rawData[index];
                    }
                    return data;
                }
            };
        }
        return record;
    }

}
