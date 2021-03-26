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
package org.openhab.binding.wmbus.config;

/**
 * Configuration of WM-Bus communication device.
 *
 * @author Łukasz Dywicki - Initial contribution
 */
public class WMBusBridgeConfig {

    public String encryptionKeys;
    public String deviceIDFilter;
    public DateFieldMode dateFieldMode = DateFieldMode.DATE_TIME;

    public int[] getDeviceIDFilter() {
        String[] ids = deviceIDFilter.split(";");
        int[] idInts = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            String curID = ids[i];
            idInts[i] = Integer.parseInt(curID);
        }
        return idInts;
    }
}
