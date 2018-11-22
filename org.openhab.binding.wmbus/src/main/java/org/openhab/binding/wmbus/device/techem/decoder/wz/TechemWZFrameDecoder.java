/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder.wz;

import org.openhab.binding.wmbus.device.techem.TechemBindingConstants;
import org.openhab.binding.wmbus.device.techem.decoder.TechemFrameDecoder;
import org.openhab.binding.wmbus.device.techem.decoder.TechemVariantFrameDecoder;

import com.google.common.collect.ImmutableMap;

public class TechemWZFrameDecoder extends TechemVariantFrameDecoder {

    private static final ImmutableMap<Byte, TechemFrameDecoder> CODEC_MAP = ImmutableMap
            .<Byte, TechemFrameDecoder> builder()
            // warm water
            .put(Byte.valueOf((byte) 0x62), new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH116255_6))
            // cold water
            .put(Byte.valueOf((byte) 0x72), new TechemWaterMeterFrameDecoder(TechemBindingConstants._68TCH116255_16))
            // heat meter
            .put(Byte.valueOf((byte) 0x43), new TechemHeatMeterFrameDecoder(TechemBindingConstants._68TCH113255_4))
            .build();

    public TechemWZFrameDecoder() {
        super(CODEC_MAP);
    }

}
