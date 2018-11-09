/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.qloud;

import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.internal.WMBusDevice;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.WMBusMessage;

/**
 * Representation of Q-lound/Fastforward device.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class QloudWMBusDevice extends WMBusDevice {

    public QloudWMBusDevice(WMBusDevice device) throws DecodingException {
        this(device.getOriginalMessage(), device.getAdapter());
    }

    public QloudWMBusDevice(WMBusMessage originalMessage, WMBusAdapter adapter) throws DecodingException {
        super(originalMessage, adapter);
    }

}
