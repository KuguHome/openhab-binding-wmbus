/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.wireless;

/**
 * The wireless M-Bus modes.
 */
public enum WMBusMode {
    /**
     * Compact (868.95 MHz). Combination of range (S) and battery efficiency (T). Suitable for frequent sending.
     */
    C,
    /**
     * Frequent (868.95 MHz). Meter sends data several times/day.
     */
    T,
    /**
     * Stationary (868.3 MHz). Meter sends data few times/day.
     */
    S;
}
