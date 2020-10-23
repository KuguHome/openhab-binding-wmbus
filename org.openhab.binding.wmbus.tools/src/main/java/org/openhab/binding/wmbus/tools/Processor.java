/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.tools;

import java.util.Map;

/**
 * Generic purpose frame processor.
 *
 * @param <T> Type of frame representation.
 */
public interface Processor<T> {

  String RSSI = "rssi";

  T process(T frame, Map<String, Object> context);

}
