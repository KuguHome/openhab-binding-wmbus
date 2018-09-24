/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
