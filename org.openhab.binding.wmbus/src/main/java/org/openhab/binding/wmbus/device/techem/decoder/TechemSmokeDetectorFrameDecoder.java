/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.TechemBindingConstants;
import org.openhab.binding.wmbus.device.techem.TechemSmokeDetector;
import org.openmuc.jmbus.SecondaryAddress;

class TechemSmokeDetectorFrameDecoder extends AbstractTechemFrameDecoder<TechemSmokeDetector> {

    TechemSmokeDetectorFrameDecoder() {
        super(TechemBindingConstants._68TCH118255_161);
    }

    @Override
    protected TechemSmokeDetector decode(WMBusDevice device, SecondaryAddress address, byte[] buffer) {
        int offset = address.asByteArray().length + 2;
        int coding = buffer[offset] & 0xFF;

        if (coding == 0xA0 || coding == 0xA1) {
            return new TechemSmokeDetector(device.getOriginalMessage(), device.getAdapter());
        }

        return null;

    }
}
