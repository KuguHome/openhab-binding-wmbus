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

public class SkipProcessor implements Processor<String> {

  private final int amount;

  public SkipProcessor(int amount) {
    this.amount = amount;
  }

  @Override
  public String process(String frame, Map<String, Object> context) {
    // one byte is 2 characters in hex representation
    return frame.substring(amount * 2);
  }
}
