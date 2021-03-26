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
package org.openhab.binding.wmbus;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Basic lookup table from 3 letter identifier code to full name of manufacturer.
 *
 * Captured from http://www.dlms.com/organization/flagmanufacturesids/index.html.
 * Last update June 2018.
 *
 * @author Łukasz Dywicki - initial contribution.
 */
@NonNullByDefault
public class WMBusCompanyIdentifiers {

    private static final Map<String, String> CIC = new HashMap<>();

    static {
        CIC.put("TCH", "TECHEM");
        CIC.put("QDS", "QUNDIS");
        CIC.put("KAM", "KAMSTRUP");
        CIC.put("ARF", "ADEUNIS");
        CIC.put("EFE", "ENGELMANN");
    }

    /**
     * Returns the company name as a String
     *
     * @param manufacturer the WMBus manufacturer identifier
     * @return The company name
     */
    public static @Nullable String get(String manufacturer) {
        if (manufacturer != null) {
            return CIC.get(manufacturer);
        } else {
            return null;
        }
    }
}
