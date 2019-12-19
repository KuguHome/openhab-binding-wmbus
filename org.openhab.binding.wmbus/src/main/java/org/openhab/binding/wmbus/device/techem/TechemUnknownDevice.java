/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device.techem;

import java.util.ArrayList;

import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openmuc.jmbus.DeviceType;
import org.openmuc.jmbus.wireless.WMBusMessage;

/**
 * The {@link TechemHeatCostAllocator} class covers heat cost allocator devices at very basic level.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class TechemUnknownDevice extends TechemHeatCostAllocator {

    public TechemUnknownDevice(WMBusMessage originalMessage, WMBusAdapter adapter, Variant variant) {
        super(originalMessage, adapter, variant, new ArrayList<>());
    }

}
