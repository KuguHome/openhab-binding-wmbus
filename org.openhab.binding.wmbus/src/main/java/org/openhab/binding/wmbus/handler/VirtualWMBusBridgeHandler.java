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

import static org.openhab.binding.wmbus.WMBusBindingConstants.THING_TYPE_VIRTUAL_BRIDGE;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.openhab.binding.wmbus.config.WMBusBridgeConfig;
import org.openhab.binding.wmbus.internal.WMBusReceiver;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.io.transport.mbus.wireless.KeyStorage;

/**
 * The {@link VirtualWMBusBridgeHandler} class defines This class represents the WMBus bridge which
 * does not ship any real connection .
 *
 * @author Hanno - Felix Wagner - Initial contribution
 * @author Łukasz Dywicki - Bringing back functionality via separate handler
 */
public class VirtualWMBusBridgeHandler extends WMBusBridgeHandlerBase {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_VIRTUAL_BRIDGE);

    public VirtualWMBusBridgeHandler(Bridge bridge, KeyStorage keyStorage) {
        super(bridge, keyStorage);
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        return Collections.emptyList(); // all good, otherwise add some messages
    }

    /**
     * Connects to the WMBus radio module and updates bridge status.
     *
     * @see org.openhab.core.thing.binding.BaseThingHandler#initialize()
     */
    @Override
    public void initialize() {
        logger.debug("WMBusBridgeHandler: initialize()");

        updateStatus(ThingStatus.UNKNOWN);
        wmbusReceiver = new WMBusReceiver(this);

        WMBusBridgeConfig config = getConfigAs(WMBusBridgeConfig.class);
        if (config.deviceIDFilter == null || config.deviceIDFilter.trim().isEmpty()) {
            logger.debug("Device ID filter is empty.");
        } else {
            wmbusReceiver.setFilterIDs(config.getDeviceIDFilter());
        }

        // success
        logger.debug("WMBusBridgeHandler: Initialization done! Setting bridge online");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        super.dispose();

        if (wmbusReceiver != null) {
            wmbusReceiver = null;
        }
    }

    public void reset() {
        wmbusReceiver = null;
        initialize();
    }
}
