/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder;

import static org.openhab.binding.wmbus.device.techem.TechemBindingConstants.*;

import com.google.common.collect.ImmutableMap;

class TechemVersionFrameDecoderSelector extends TechemVariantFrameDecoderSelector {

    private static final ImmutableMap<Byte, TechemFrameDecoder<?>> CODEC_MAP = ImmutableMap
            .<Byte, TechemFrameDecoder<?>> builder()
            .put(Byte.valueOf((byte) 0x45), new TechemHKVFrameDecoder(_68TCH6967_8))
            .put(Byte.valueOf((byte) 0x61), new TechemHKVFrameDecoder(_68TCH97255_8))
            .put(Byte.valueOf((byte) 0x64), new TechemHKVFrameDecoder(_68TCH100128_8))
            .put(Byte.valueOf((byte) 0x69), new TechemHKVRoomTempFrameDecoder(_68TCH105128_8, 0, 1))
            .put(Byte.valueOf((byte) 0x94), new TechemHKVRoomTempFrameDecoder(_68TCH148128_8, 1, 1))
            .put(Byte.valueOf((byte) 0x76),
                    new TechemSmokeDetectorFrameDecoder(_68TCH118255_161_A0, _68TCH118255_161_A1))

            .put(Byte.valueOf((byte) 0x22), new TechemHeatMeterFrameDecoder(_68TCH3467_4))
            .put(Byte.valueOf((byte) 0x39), new TechemHeatMeterFrameDecoder(_68TCH5767_4))
            .put(Byte.valueOf((byte) 0x71), new TechemHeatMeterFrameDecoder(_68TCH11367_4_A0, _68TCH11367_4_A2))
            .put(Byte.valueOf((byte) 0x57), new TechemHeatMeterFrameDecoder(_68TCH8768_4))

            .build();

    TechemVersionFrameDecoderSelector() {
        super(BASED_ON_VERSION, CODEC_MAP);
    }
}