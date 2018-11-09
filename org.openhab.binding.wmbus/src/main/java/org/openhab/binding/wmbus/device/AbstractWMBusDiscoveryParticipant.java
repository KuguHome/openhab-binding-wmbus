/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wmbus.discovery.WMBusDiscoveryParticipant;
import org.openhab.binding.wmbus.internal.WMBusDevice;

/**
 * Base class for discovery participants.
 *
 * @author Łukasz Dywicki - initial contribution
 */
public abstract class AbstractWMBusDiscoveryParticipant implements WMBusDiscoveryParticipant {

    @Override
    public @Nullable ThingUID getThingUID(WMBusDevice device) {
        return new ThingUID(getThingType(), getDeviceID(device));
    }

    private String getDeviceID(WMBusDevice device) {
        return device.getDeviceId();
    }

    protected abstract ThingTypeUID getThingType();

}
