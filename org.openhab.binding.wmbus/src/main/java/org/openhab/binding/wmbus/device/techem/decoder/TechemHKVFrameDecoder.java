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
package org.openhab.binding.wmbus.device.techem.decoder;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.Record;
import org.openhab.binding.wmbus.device.techem.TechemHeatCostAllocator;
import org.openhab.binding.wmbus.device.techem.TechemUnknownDevice;
import org.openhab.binding.wmbus.device.techem.Variant;
import org.openmuc.jmbus.DeviceType;
import org.openmuc.jmbus.SecondaryAddress;

class TechemHKVFrameDecoder extends AbstractTechemFrameDecoder<TechemHeatCostAllocator> {

    protected final boolean reportsTemperature;

    TechemHKVFrameDecoder(Variant variant) {
        this(variant, false);
    }

    TechemHKVFrameDecoder(Variant variant, boolean temperature) {
        super(variant);
        this.reportsTemperature = temperature;
    }

    @Override
    protected TechemHeatCostAllocator decode(WMBusDevice device, SecondaryAddress address, byte[] buffer) {
        Buffer buff = new Buffer(device.getOriginalMessage(), address);

        int coding = buff.skip(2).readByte() & 0xFF;
        if (variant.getCoding() == coding) {

            List<Record<?>> records = new ArrayList<>();
            records.add(new Record<>(Record.Type.STATUS, ((Byte) buff.readByte()).intValue()));
            records.add(new Record<>(Record.Type.PAST_READING_DATE, buff.readPastDate()));
            records.add(new Record<>(Record.Type.PAST_VOLUME, (float) buff.readShort()));
            records.add(new Record<>(Record.Type.CURRENT_READING_DATE, buff.readCurrentDate()));
            records.add(new Record<>(Record.Type.CURRENT_VOLUME, (float) buff.readShort()));
            records.add(new Record<>(Record.Type.RSSI, device.getOriginalMessage().getRssi()));
            records.add(new Record<>(Record.Type.ALMANAC, ""));

            return new TechemHeatCostAllocator(device.getOriginalMessage(), device.getAdapter(), variant, records);
        }

        if (coding == 0xA3) {
            return new TechemUnknownDevice(device.getOriginalMessage(), device.getAdapter(),
                    new Variant(variant.version, variant.reportedType, coding, DeviceType.UNKNOWN));
        }

        return null;
    }
}
