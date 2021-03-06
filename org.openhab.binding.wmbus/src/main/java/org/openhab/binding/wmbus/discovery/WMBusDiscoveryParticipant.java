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

import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

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
    Set<ThingTypeUID> getSupportedThingTypeUIDs();

    /**
     * Creates a discovery result for a WMBus device
     *
     * @param device the WMbus device found on the network
     * @return the according discovery result or <code>null</code>, if device is not
     *         supported by this participant
     */
    @Nullable
    DiscoveryResult createResult(WMBusDevice device);

    /**
     * Returns the thing UID for a WMBus device
     *
     * @param device the WMBus device
     * @return a thing UID or <code>null</code>, if the device is not supported by this participant
     */
    @Nullable
    public ThingUID getThingUID(WMBusDevice device);
}
