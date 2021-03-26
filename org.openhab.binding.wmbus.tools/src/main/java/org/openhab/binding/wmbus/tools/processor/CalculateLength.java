/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.tools.processor;

import java.util.Map;

import org.openhab.binding.wmbus.tools.Processor;

public class CalculateLength implements Processor<String> {

    @Override
    public String process(String frame, Map<String, Object> context) {
        // remember of hex notation which doubles length
        Integer len = frame.length() / 2;
        return Integer.toHexString(len) + frame;
    }
}
