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

public class Record<T> {

    public enum Type {
        CURRENT_VOLUME,
        CURRENT_READING_DATE(true),
        CURRENT_READING_DATE_SMOKE(true),
        PAST_VOLUME,
        PAST_READING_DATE(true),
        ROOM_TEMPERATURE,
        RADIATOR_TEMPERATURE,
        RSSI,
        ALMANAC,
        STATUS,
        COUNTER;

        private final boolean dateField;

        Type() {
            this(false);
        }

        Type(boolean dateField) {
            this.dateField = dateField;
        }

        public boolean isDate() {
            return dateField;
        }
    }

    private final Type type;
    private final T value;

    public Record(Type type, T value) {
        this.type = type;
        this.value = value;
    }

    public final Type getType() {
        return this.type;
    }

    public final T getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "Record [" + type + ", " + value + "]";
    }
}
