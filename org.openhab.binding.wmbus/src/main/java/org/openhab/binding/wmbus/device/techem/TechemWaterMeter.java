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

package org.openhab.binding.wmbus.device.techem;

import java.util.List;

import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openmuc.jmbus.wireless.WMBusMessage;

/**
 * The {@link TechemWaterMeter} class covers basic water measurement devices.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */

public class TechemWaterMeter extends TechemDevice {

    public TechemWaterMeter(WMBusMessage originalMessage, WMBusAdapter adapter, Variant variant,
            List<Record<?>> measures) {
        super(originalMessage, adapter, variant, measures);
    }
}
