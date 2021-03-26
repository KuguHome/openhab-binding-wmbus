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
package org.openhab.binding.wmbus.device;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wmbus.BindingConfiguration;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.discovery.WMBusDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Base class for discovery participants.
 *
 * @author Łukasz Dywicki - initial contribution
 */
public abstract class AbstractWMBusDiscoveryParticipant implements WMBusDiscoveryParticipant {

    private BindingConfiguration configuration;

    @Override
    public @Nullable ThingUID getThingUID(WMBusDevice device) {
        if (configuration.getIncludeBridgeUID()) {
            return new ThingUID(getThingType(device), device.getAdapter().getUID(), getDeviceID(device));
        } else {
            return new ThingUID(getThingType(device), getDeviceID(device));
        }
    }

    private String getDeviceID(WMBusDevice device) {
        return device.getDeviceId();
    }

    protected abstract ThingTypeUID getThingType(WMBusDevice device);

    public void setBindingConfiguration(BindingConfiguration configuration) {
        this.configuration = configuration;
    }

    public void unsetBindingConfiguration(BindingConfiguration configuration) {
        this.configuration = null;
    }

    protected Long getTimeToLive() {
        return configuration.getTimeToLive();
    }
}
