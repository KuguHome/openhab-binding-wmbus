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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.config.DateFieldMode;
import org.openhab.core.common.registry.Identifiable;
import org.openhab.core.thing.ThingUID;

/**
 * Representation of WMBus device which is holds a link to radio device.
 * <p>
 * Main purpose of this interface is to cut off hard dependency on {@link WMBusBridgeHandler}.
 *
 * @author Łukasz Dywicki
 */
@NonNullByDefault
public interface WMBusAdapter extends Identifiable<ThingUID> {

    void processMessage(WMBusDevice device);

    void reset();

    DateFieldMode getDateFieldMode();
}
