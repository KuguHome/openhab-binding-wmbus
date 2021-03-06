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

package org.openhab.binding.wmbus.device.techem;

import java.util.List;
import java.util.Optional;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.Record.Type;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openmuc.jmbus.DeviceType;
import org.openmuc.jmbus.wireless.WMBusMessage;

/**
 * The {@link TechemDevice} groups devices manufactured by Techem.
 *
 * @author Łukasz Dywicki - Initial contribution
 */
public class TechemDevice extends WMBusDevice {

    private final List<Record<?>> measurements;
    private final Variant variant;

    protected TechemDevice(WMBusMessage originalMessage, WMBusAdapter adapter, Variant variant,
            List<Record<?>> measurements) {
        super(originalMessage, adapter);
        this.variant = variant;
        this.measurements = measurements;
    }

    public final DeviceType getTechemDeviceType() {
        return variant.getDesiredWMBusType();
    }

    public Variant getDeviceVariant() {
        return variant;
    }

    @Override
    public String getDeviceType() {
        return variant.getTechemType();
    }

    public List<Record<?>> getMeasurements() {
        return measurements;
    }

    public Optional<Record<?>> getRecord(Type type) {
        return measurements.stream().filter(record -> record.getType().equals(type)).findFirst();
    }
}
