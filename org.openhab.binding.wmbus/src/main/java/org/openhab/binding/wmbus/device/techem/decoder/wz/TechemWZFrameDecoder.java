/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder.wz;

import org.openhab.binding.wmbus.device.techem.decoder.TechemFrameDecoder;
import org.openhab.binding.wmbus.device.techem.decoder.TechemVariantFrameDecoder;
import org.openmuc.jmbus.DeviceType;

import com.google.common.collect.ImmutableMap;

public class TechemWZFrameDecoder extends TechemVariantFrameDecoder {

    private static final ImmutableMap<Byte, TechemFrameDecoder> CODEC_MAP = ImmutableMap
            .<Byte, TechemFrameDecoder> builder()
            // warm water
            .put(Byte.valueOf((byte) 0x62), new TechemWaterMeterFrameDecoder((byte) 0x62, DeviceType.WARM_WATER_METER))
            // cold water
            .put(Byte.valueOf((byte) 0x72), new TechemWaterMeterFrameDecoder((byte) 0x72, DeviceType.COLD_WATER_METER))
            // heat meter
            .put(Byte.valueOf((byte) 0x43), new TechemHeatMeterFrameDecoder()).build();

    final static String DEVICE_VARIANT = "68TCH116255";

    public TechemWZFrameDecoder() {
        super(DEVICE_VARIANT, CODEC_MAP);
    }

}
