/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder;

import org.openhab.binding.wmbus.device.techem.TechemBindingConstants;

import com.google.common.collect.ImmutableMap;

class TechemVersionFrameDecoderSelector extends TechemVariantFrameDecoderSelector {

    private static final ImmutableMap<Byte, TechemFrameDecoder<?>> CODEC_MAP = ImmutableMap
            .<Byte, TechemFrameDecoder<?>> builder()
            .put(Byte.valueOf((byte) 0x45), new TechemHKVFrameDecoder(TechemBindingConstants._68TCH6967_8))
            .put(Byte.valueOf((byte) 0x61), new TechemHKVFrameDecoder(TechemBindingConstants._68TCH97255_8))
            .put(Byte.valueOf((byte) 0x64), new TechemHKVFrameDecoder(TechemBindingConstants._68TCH100128_8))
            .put(Byte.valueOf((byte) 0x69), new TechemHKVFrameDecoder(TechemBindingConstants._68TCH105128_8, true))
            .put(Byte.valueOf((byte) 0x94),
                    new TechemHKVFrameDecoder(TechemBindingConstants._68TCH148128_8, true, COMPLEX_SHIFT_HKV94))
            .put(Byte.valueOf((byte) 0x76), new TechemSmokeDetectorFrameDecoder())

            .put(Byte.valueOf((byte) 0x71), new TechemHeatMeterFrameDecoder(TechemBindingConstants._68TCH11367_4))

            .build();

    TechemVersionFrameDecoderSelector() {
        super(BASED_ON_VERSION, CODEC_MAP);
    }
}