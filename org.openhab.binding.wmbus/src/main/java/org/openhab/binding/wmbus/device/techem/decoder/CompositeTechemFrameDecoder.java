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
import org.openhab.binding.wmbus.device.techem.TechemBindingConstants;
import org.openhab.binding.wmbus.device.techem.TechemDevice;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

@Component
public class CompositeTechemFrameDecoder implements TechemFrameDecoder<TechemDevice> {

    private final Logger logger = LoggerFactory.getLogger(CompositeTechemFrameDecoder.class);

    private final List<TechemFrameDecoder<?>> decoders = ImmutableList.of(new TechemVersionFrameDecoderSelector(),
            // warm water
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH11298_6),
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH11698_6),
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH14998_6),
            // cold water
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH112114_16),
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH116114_16),
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH149114_16));

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
        TechemDevice result = null;
        // TODO failing test: wrong water meter returned?
        for (TechemFrameDecoder<?> decoder : decoders) {
            if (decoder.suports(device.getRawDeviceType())) {
                // same variant might be supported by multiple decoders
                logger.debug("Found decoder capable of handling device {}: {}", device.getRawDeviceType(), decoder);
                result = decoder.decode(device);
                logger.debug("Decoding result: {}, {}, {}", result, result.getRawDeviceType(),
                        result.getTechemDeviceType());
            }
        }
        if (result != null) {
            return result;
        }

        logger.debug("Could not find decoder capable of handling device {}", device.getRawDeviceType());

        return null;
    }

}
