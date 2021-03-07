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
package org.openhab.binding.wmbus.device.itron;

import java.util.BitSet;

import org.openhab.binding.wmbus.device.techem.decoder.Buffer;

public class ItronConfigStatusDataParser {

    private final Byte billing;
    private final BitSet status;
    private final Byte product;
    private final Byte battery;
    private final byte[] sdErrors;
    private final Byte modemErrors;
    private final Byte config;

    ItronConfigStatusDataParser(byte[] buffer) {
        this(new Buffer(buffer));
    }

    ItronConfigStatusDataParser(Buffer buffer) {
        this.billing = buffer.readByte();
        this.status = BitSet.valueOf(new byte[] { buffer.readByte() });
        this.product = buffer.readByte();
        this.battery = buffer.readByte();
        this.sdErrors = buffer.readBytes(2);
        this.modemErrors = buffer.readByte();
        this.config = buffer.readByte();
    }

    public int getBillingDate() {
        return billing.intValue();
    }

    public boolean isRemovalOccurred() {
        return status.get(0);
    }

    public boolean isProductInstalled() {
        return status.get(1);
    }

    public int getOperationMode() {
        int value = status.toByteArray()[0];
        return value > 3 ? (value >> 2) ^ 4 : value >> 2;
    }

    public boolean isPerimeterIntrusionOccurred() {
        return status.get(4);
    }

    public boolean isSmokeInletBlockedOccurred() {
        return status.get(5);
    }

    public boolean isOutOfRangeTemperatureOccurred() {
        return status.get(6);
    }

    public byte getProductCode() {
        return product;
    }

    public int getBatteryLifetime() {
        return (battery & 0xFF) >> 1;
    }
}
