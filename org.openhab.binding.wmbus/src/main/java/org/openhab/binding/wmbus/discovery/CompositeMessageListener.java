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
package org.openhab.binding.wmbus.discovery;

import java.util.LinkedHashSet;
import java.util.Set;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.handler.WMBusMessageListener;

/**
 * Listener backed by given set of other listeners.
 *
 * @author Łukasz Dywicki - initial contribution
 */
public class CompositeMessageListener implements WMBusMessageListener {

    private final Set<WMBusMessageListener> listeners;

    public CompositeMessageListener() {
        this(new LinkedHashSet<>());
    }

    public CompositeMessageListener(Set<WMBusMessageListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onNewWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {
        for (WMBusMessageListener listener : listeners) {
            listener.onNewWMBusDevice(adapter, device);
        }
    }

    @Override
    public void onChangedWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {
        for (WMBusMessageListener listener : listeners) {
            listener.onChangedWMBusDevice(adapter, device);
        }
    }

    public void addMessageListener(WMBusMessageListener listener) {
        this.listeners.add(listener);
    }

    public void removeMessageListener(WMBusMessageListener listener) {
        this.listeners.add(listener);
    }
}
