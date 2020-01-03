/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
