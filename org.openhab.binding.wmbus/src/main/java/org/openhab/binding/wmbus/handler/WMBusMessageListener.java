/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.handler;

import org.openhab.binding.wmbus.WMBusDevice;

/**
 * The {@link WMBusMessageListener} class defines interface WMBusMessageListener
 *
 * @author Hanno - Felix Wagner - Initial contribution
 */

public interface WMBusMessageListener {

    /**
     *
     * @param adapter Adapter which received message.
     * @param device The message which was received.
     */
    public void onNewWMBusDevice(WMBusAdapter adapter, WMBusDevice device);

    /**
     *
     * @param adapter WMBusAdapter adapter who discovered change,
     * @param device The message which was received.
     */
    public void onChangedWMBusDevice(WMBusAdapter adapter, WMBusDevice device);

}
