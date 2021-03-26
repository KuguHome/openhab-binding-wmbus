/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.tools;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.handler.WMBusMessageListener;
import org.openhab.core.util.HexUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very basic message listener which drops byte representation of frame to log.
 *
 * @author Łukasz Dywicki - Initial contribution and API.
 */
@Component(immediate = true)
public class LoggingMessageListener implements WMBusMessageListener {

    private final Logger logger = LoggerFactory.getLogger(LoggingMessageListener.class);

    @Override
    public void onNewWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {
        log(device);
    }

    @Override
    public void onChangedWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {
        log(device);
    }

    private void log(WMBusDevice device) {
        logger.debug(HexUtils.bytesToHex(device.getOriginalMessage().asBlob()));
    }
}
