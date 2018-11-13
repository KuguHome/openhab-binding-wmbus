/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.handler;

import org.eclipse.smarthome.core.common.registry.Identifiable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wmbus.WMBusDevice;

/**
 * Representation of WMBus device which is holds a link to radio device.
 *
 * Main purpose of this interface is to cut off hard dependency on {@link WMBusBridgeHandler}.
 *
 * @author Łukasz Dywicki
 */
public interface WMBusAdapter extends Identifiable<ThingUID> {

    void processMessage(WMBusDevice device);

}
