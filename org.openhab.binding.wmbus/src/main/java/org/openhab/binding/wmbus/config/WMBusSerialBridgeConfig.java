/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.config;

import org.openmuc.jmbus.wireless.WMBusMode;

/**
 * A specialized version of configuration of serial devices - USB sticks.
 *
 * @author Łukasz Dywicki - Initial contribution
 */
public class WMBusSerialBridgeConfig extends WMBusBridgeConfig {

    public StickModel stickModel;
    public String serialDevice;
    public WMBusMode radioMode;

}
