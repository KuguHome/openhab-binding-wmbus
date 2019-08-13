/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder;

import java.util.List;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.TechemDevice;
import org.openhab.binding.wmbus.device.techem.decoder.hkv.TechemHKV45FrameDecoder;
import org.openhab.binding.wmbus.device.techem.decoder.hkv.TechemHKV69FrameDecoder;
import org.openhab.binding.wmbus.device.techem.decoder.sd.TechemSD76FrameDecoder;
import org.openhab.binding.wmbus.device.techem.decoder.hkv.TechemHKVFrameDecoder;
import org.openhab.binding.wmbus.device.techem.decoder.wz.TechemWZFrameDecoder;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

@Component
public class CompositeTechemFrameDecoder implements TechemFrameDecoder<TechemDevice> {

    private final Logger logger = LoggerFactory.getLogger(CompositeTechemFrameDecoder.class);

    private final List<TechemFrameDecoder<?>> decoders = ImmutableList.of(new TechemHKVFrameDecoder(),
            new TechemSD76FrameDecoder(), new TechemHKV69FrameDecoder(), new TechemHKV45FrameDecoder(),
            new TechemWZFrameDecoder());

    @Override
    public boolean suports(String deviceVariant) {
        for (TechemFrameDecoder<?> decoder : decoders) {
            if (decoder.suports(deviceVariant)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public TechemDevice decode(WMBusDevice device) {
        if (device instanceof TechemDevice) {
            return (TechemDevice) device;
        }

        for (TechemFrameDecoder<?> decoder : decoders) {
            if (decoder.suports(device.getDeviceType())) {
                // same variant might be supported by multiple decoders
                logger.debug("Found parser capable handling device {}, {}", device.getDeviceType(), decoder);
                TechemDevice result = decoder.decode(device);
                if (result != null) {
                    return result;
                }
            }
        }

        logger.debug("Could not find parser capable handling device {}", device.getDeviceType());

        return null;
    }

}
