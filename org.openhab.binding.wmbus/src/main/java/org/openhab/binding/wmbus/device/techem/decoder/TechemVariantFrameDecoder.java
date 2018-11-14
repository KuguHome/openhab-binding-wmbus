/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder;

import java.util.Map;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.TechemDevice;
import org.openmuc.jmbus.SecondaryAddress;

public class TechemVariantFrameDecoder extends AbstractTechemFrameDecoder<TechemDevice> {

    private final Map<Byte, TechemFrameDecoder> decoders;
    private final int tagOffset;

    protected TechemVariantFrameDecoder(String variant, Map<Byte, TechemFrameDecoder> codecMap) {
        this(variant, 1, codecMap);
    }

    protected TechemVariantFrameDecoder(String variant, int tagOffset, Map<Byte, TechemFrameDecoder> codecMap) {
        super(variant);
        this.tagOffset = tagOffset;
        this.decoders = codecMap;
    }

    @Override
    protected TechemDevice decode(WMBusDevice device, SecondaryAddress address, byte[] buffer) {
        int tagIndex = address.asByteArray().length + tagOffset;

        if (buffer.length <= tagIndex) {
            return null;
        }

        Byte tag = buffer[tagIndex];
        if (decoders.containsKey(tag)) {
            return decoders.get(tag).decode(device);
        }

        return null;
    }

}
