/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder.hkv;

import org.openhab.binding.wmbus.device.techem.TechemBindingConstants;

public class TechemHKV45FrameDecoder extends AbstractTechemHKVFrameDecoder {

    public TechemHKV45FrameDecoder() {
        super(TechemBindingConstants._68TCH69255_8, false);
    }
}
