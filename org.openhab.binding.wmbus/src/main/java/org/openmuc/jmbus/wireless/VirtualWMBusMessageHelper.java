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

package org.openmuc.jmbus.wireless;

import java.util.Map;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.SecondaryAddress;

/**
 * The {@link VirtualWMBusMessageHelper} class defines VirtualWMBusMessageHelper
 *
 * @author Roman Malyugin - Initial contribution
 */
public class VirtualWMBusMessageHelper {

    public static WMBusMessage decode(byte[] buffer, Integer signalStrengthInDBm, Map<SecondaryAddress, byte[]> keyMap)
            throws DecodingException {
        return WMBusMessage.decode(buffer, signalStrengthInDBm, keyMap);
    }
}
