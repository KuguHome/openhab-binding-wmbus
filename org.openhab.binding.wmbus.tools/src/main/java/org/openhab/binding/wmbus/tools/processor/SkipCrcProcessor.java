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

public class SkipCrcProcessor implements Processor<String> {

  @Override
  public String process(String frame, Map<String, Object> context) {
    String strippedframe = "";

      strippedframe += frame.substring(Math.min(0, frame.length()), Math.min(18, frame.length()));
      strippedframe += frame.substring(Math.min(22, frame.length()), Math.min(54, frame.length()));
      strippedframe += frame.substring(Math.min(58, frame.length()), Math.min(90, frame.length()));

    // String strippedframe = frame.substring(0, 18) + frame.substring(22, frame.length());
    /*
     * TODO: general implementation
     *
     *
     * Integer position = 2;
     * while (position < frame.length()) {
     * strippedframe += frame.substring(position, position+16);
     * position += 16 ;
     * }
     */
    return strippedframe;
  }

}
