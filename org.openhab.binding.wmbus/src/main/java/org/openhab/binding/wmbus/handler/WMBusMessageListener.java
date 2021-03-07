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
