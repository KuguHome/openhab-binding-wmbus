/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.config;

/**
 * Setting describing possible modes for handling WMBus date/date time fields and their mapping back to openhab universe
 * through binding code.
 *
 * @author Łukasz Dywicki - Initial contribution
 */
public enum DateFieldMode {

    FORMATTED_STRING,
    UNIX_TIMESTAMP,
    DATE_TIME

}
