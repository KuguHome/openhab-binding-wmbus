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

/**
 * Frame decoder which passes actual work to delegate after confirming matching device variant.
 *
 * Since Techem devices tend to use very similar wmbus identification and most of them reports themselves as OTHER we
 * need to determine its type based on device version. This decoder reads byte at fixed, given position and then try to
 * match given decoders with it.
 * Since standard leaves space for vendor specific device types this type is an attempt to sort it in a way which does
 * not interfere with actual frame decoding logic.
 * Tag offset is calculated from end of address field which contains manufacturer, device id, device version and device
 * type. Value -1 passed to constructor indicates that variant determination is based on device version while value 0
 * points to last byte in address which is device type.
 *
 * @author Łukasz Dywicki - initial contribution
 */
public class TechemVariantFrameDecoder extends AbstractTechemFrameDecoder<TechemDevice> {

    private final Map<Byte, TechemFrameDecoder> decoders;
    private final int tagOffset;

    protected TechemVariantFrameDecoder(String variant, Map<Byte, TechemFrameDecoder> codecMap) {
        this(variant, 0, codecMap);
    }

    protected TechemVariantFrameDecoder(String variant, int tagOffset, Map<Byte, TechemFrameDecoder> codecMap) {
        super(variant);
        this.tagOffset = tagOffset;
        this.decoders = codecMap;
    }

    @Override
    protected TechemDevice decode(WMBusDevice device, SecondaryAddress address, byte[] buffer) {
        byte[] addressArray = address.asByteArray();
        int tagIndex = addressArray.length + tagOffset - 1;

        if (addressArray.length <= tagIndex) {
            return null;
        }

        Byte tag = addressArray[tagIndex];
        if (decoders.containsKey(tag)) {
            return decoders.get(tag).decode(device);
        }

        return null;
    }

}
