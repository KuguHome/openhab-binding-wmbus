/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder;

import java.time.LocalDateTime;
import java.util.function.Function;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusMessage;

/**
 * Buffer implementation which dumps contents of frame during processing so it is easier to spot how payload is utilized
 * by various parsers.
 *
 * @author Łukasz Dywicki - initial contribution.
 */
public class DebugBuffer extends Buffer {

    public DebugBuffer(WMBusMessage message) {
        this(message.asBlob(), message.getSecondaryAddress());
    }

    public DebugBuffer(WMBusMessage message, SecondaryAddress secondaryAddress) {
        this(message.asBlob(), secondaryAddress);
    }

    public DebugBuffer(byte[] buffer, SecondaryAddress secondaryAddress) {
        this(buffer, secondaryAddress.asByteArray().length);
    }

    public DebugBuffer(byte[] buffer) {
        this(buffer, 0);
    }

    public DebugBuffer(byte[] buffer, int offset) {
        super(buffer, offset);
        logger.info("Skipped first {} bytes {}", offset, dump());
    }

    public DebugBuffer skip(int bytes) {
        logger.info("Attempting to move reader index from {} by {} byte(s) {}", position(), bytes, dump());
        super.skip(bytes);
        return this;
    }

    public byte readByte() {
        logger.info("Read byte {}", dump());
        return super.readByte();
    }

    public int readInt() {
        logger.info("Read 3 byte integer from {}", dump());
        return super.readInt();
    }

    public short readShort() {
        logger.info("Read 2 byte (short) from {}", dump());
        return super.readShort();
    }

    public float readFloat() {
        logger.info("Read 2 byte (float) from {}", dump());
        return super.readFloat();
    }

    public LocalDateTime readPastDate() {
        logger.info("Read 2 bytes (short) and turn int into data {}", dump());
        return super.readPastDate();
    }

    public LocalDateTime readCurrentDate() {
        logger.info("Read 2 bytes (short) and turn int into data {}", dump());
        return super.readCurrentDate();
    }

    public float readFloat(Function<Float, Float> scale) {
        logger.info("Read 2 bytes (short) and turn int into float and rescale {}", dump());
        return super.readFloat(scale);
    }

    private final String dump() {
        byte[] array = buffer.array();

        String output = "\nBuffer size: " + array.length + ", read index: " + buffer.position() + "\n";
        String middleLine = "";
        String lowerLine = "";
        for (int index = 0; index < array.length; index++) {
            output += String.format("%02X", array[index]) + " ";
            if (index < 10 && index != 0) {
                lowerLine += "  ";
            } else {
                lowerLine += " ";
            }
            lowerLine += index;
            if (index == buffer.position()) {
                middleLine += " ^ ";
            } else {
                middleLine += "   ";
            }
        }

        return output + "\n" + lowerLine + "\n" + middleLine;
    }

}
