/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.discovery;

import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wmbus.WMBusDevice;

/**
 * Discovery participant which is responsible for identification of WMBus device.
 *
 * Realizations of this interface might return empty results when they do not know device in order to let other
 * participants do their job.
 *
 * @author Łukasz Dywicki - initial contribution.
 */
public interface WMBusDiscoveryParticipant {

    /**
     * Defines the list of thing types that this participant can identify
     *
     * @return a set of thing type UIDs for which results can be created
     */
    public Set<ThingTypeUID> getSupportedThingTypeUIDs();

    /**
     * Creates a discovery result for a WMBus device
     *
     * @param device the WMbus device found on the network
     * @return the according discovery result or <code>null</code>, if device is not
     *         supported by this participant
     */
    @Nullable
    public DiscoveryResult createResult(WMBusDevice device);

    /**
     * Returns the thing UID for a WMBus device
     *
     * @param device the WMBus device
     * @return a thing UID or <code>null</code>, if the device is not supported by this participant
     */
    @Nullable
    public ThingUID getThingUID(WMBusDevice device);

}
