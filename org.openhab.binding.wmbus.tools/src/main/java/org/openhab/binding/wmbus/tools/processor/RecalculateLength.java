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

public class RecalculateLength implements Processor<String> {

    @Override
    public String process(String frame, Map<String, Object> context) {
        int inputByteLength = frame.length() / 2;

        String newFrame = "";
        Integer numberOfBlocks;
        int ciField = parseInt(frame, 20, 22);

        if (ciField == 0x7A) {
            numberOfBlocks = (inputByteLength - 14) / 16;
            inputByteLength = 14 + numberOfBlocks * 16;
            newFrame += Integer.toHexString(inputByteLength);
            newFrame += frame.substring(2, 26);
            newFrame += Integer.toHexString(numberOfBlocks * 16);
            newFrame += frame.substring(28, (inputByteLength * 2) + 2);
            return newFrame;
        }

        if (ciField == 0x72) {
            numberOfBlocks = (inputByteLength - 22) / 16;
            inputByteLength = 22 + numberOfBlocks * 16;
            newFrame += Integer.toHexString(inputByteLength);
            newFrame += frame.substring(2, 42);
            newFrame += Integer.toHexString(numberOfBlocks * 16);
            newFrame += frame.substring(44, (inputByteLength * 2) + 2);
            return newFrame;
        }

        return newFrame;
    }

    private int parseInt(String frame, int startIndex, int endIndex) {
        return Integer.parseInt(frame.substring(startIndex, endIndex), 16);
    }
}
