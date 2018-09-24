/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.handler;

import org.openhab.binding.wmbus.internal.WMBusDevice;

/**
 * The {@link WMBusMessageListener} class defines interface WMBusMessageListener
 *
 * @author Hanno - Felix Wagner - Initial contribution
 */

public interface WMBusMessageListener {

    /**
     *
     * @param device The message which was received.
     */
    public void onNewWMBusDevice(WMBusDevice device);

    /**
     *
     * @param device The message which was received.
     */
    public void onChangedWMBusDevice(WMBusDevice device);

}
