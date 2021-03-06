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
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH11298_6, 1),
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH11698_6, 1),
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH14998_6, -1),
            // cold water
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH112114_16, -1),
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH116114_16, -1),
            new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH149114_16, -1));

    @Override
    public boolean supports(String deviceVariant) {
        for (TechemFrameDecoder<?> decoder : decoders) {
            if (decoder.supports(deviceVariant)) {
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
            if (decoder.supports(device.getRawDeviceType())) {
                // same variant might be supported by multiple decoders, but first one which gives decoded result wins
                logger.debug("Found decoder capable of handling device {}: {}", device.getRawDeviceType(),
                        decoder.getClass().getName());
                result = decoder.decode(device);
                if (result != null) {
                    logger.debug("Decoding result: {}, {}, {}", result, result.getRawDeviceType(),
                            result.getTechemDeviceType());
                    break;
                } else {
                    logger.debug("Decoding of frame failed, unsupported device variant");
                }
            }
        }
        if (result != null) {
            return result;
        }

        logger.debug("Could not find decoder capable of handling device {}", device.getRawDeviceType());

        return null;
    }
}
