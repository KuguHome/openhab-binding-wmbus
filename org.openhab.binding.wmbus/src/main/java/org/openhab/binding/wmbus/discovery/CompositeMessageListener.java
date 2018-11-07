/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.discovery;

import java.util.LinkedHashSet;
import java.util.Set;

import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.handler.WMBusMessageListener;
import org.openhab.binding.wmbus.internal.WMBusDevice;

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
