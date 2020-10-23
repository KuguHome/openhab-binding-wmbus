/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.tools.processor;

import org.openhab.binding.wmbus.tools.Processor;

import java.util.Map;

public class RssiProcessor implements Processor<String> {

  private final int rssiIndex;
  private final int rssiValue;

  public RssiProcessor(int rssiIndex, int rssiValue) {
    this.rssiIndex = rssiIndex;
    this.rssiValue = rssiValue;
  }

  @Override
  public String process(String frame, Map<String, Object> context) {
    if (rssiIndex != 0) {
      if (rssiIndex == 1) { // first byte starts from character at index 0.
        context.put(RSSI, Integer.parseUnsignedInt(frame.substring(0, 2), 16));
        return frame.substring(2);
      } else if (rssiIndex == -1) {
        context.put(RSSI, Integer.parseUnsignedInt(frame.substring(frame.length() -2), 16));
        return frame.substring(0, frame.length() - 2);
      }
    }

    context.put(RSSI, rssiValue);
    return frame;
  }
}
