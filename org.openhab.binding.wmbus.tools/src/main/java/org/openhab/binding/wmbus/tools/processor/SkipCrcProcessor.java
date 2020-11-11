/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.tools.processor;

import org.openhab.binding.wmbus.tools.Processor;

import java.util.HashMap;
import java.util.Map;

public class SkipCrcProcessor implements Processor<String> {

  @Override
  public String process(String frame, Map<String, Object> context) {
    String strippedframe = "";
    int len = frame.length() / 2; // include all fields

    //int lengthField = Integer.decode(frame.substring(0, 2));
    int ciField = parseInt(frame, 22, 24);
    boolean formatB = ciField == 0x72 || ciField == 0x7A;

    if (formatB) {
      strippedframe += frame.substring(0, Math.min(18, frame.length() - 4));
      // block 1 + block 2 = 11 + ((16*n)+2) -- max 129 bytes
      if (len <= 129) {
        strippedframe += frame.substring(Math.min(22, frame.length()), frame.length() - 4);
      }
      // block 1 + block 2 + block 3= 11 + ((16*n)+2) + (16*n) -- bytes > 129
      else {
        strippedframe += frame.substring(22, 256);
        strippedframe += frame.substring(Math.min(260, frame.length()), frame.length() - 4);
      }
    } else {
      // assume that we starts counting from C-field
      strippedframe += frame.substring(0, Math.min(18, frame.length()));
      int position = 22;
      while (position < frame.length()) {
        strippedframe += frame.substring(position, Math.min(position + 32, frame.length()));
        position += 36;
      }
      strippedframe = strippedframe.substring(0,strippedframe.length()-4);
    }

    return strippedframe;
  }

  private int parseInt(String frame, int startIndex, int endIndex) {
    return Integer.parseInt(frame.substring(startIndex, endIndex), 16);
  }

}
