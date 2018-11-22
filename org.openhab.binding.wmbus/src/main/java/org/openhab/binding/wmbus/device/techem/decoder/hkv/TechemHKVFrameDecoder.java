/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder.hkv;

import org.openhab.binding.wmbus.device.techem.decoder.TechemFrameDecoder;
import org.openhab.binding.wmbus.device.techem.decoder.TechemVariantFrameDecoder;

import com.google.common.collect.ImmutableMap;

public class TechemHKVFrameDecoder extends TechemVariantFrameDecoder {

    private static final ImmutableMap<Byte, TechemFrameDecoder> CODEC_MAP = ImmutableMap
            .<Byte, TechemFrameDecoder> builder()
            // we have no test frame, but that's all difference between device variants
            .put(Byte.valueOf((byte) 0x61), new TechemHKV61FrameDecoder())
            // should be pretty much same as above
            .put(Byte.valueOf((byte) 0x64), new TechemHKV64FrameDecoder()).build();

    final static String DEVICE_VARIANT = "68TCH100255";

    public TechemHKVFrameDecoder() {
        super(-1, CODEC_MAP);
    }

}
