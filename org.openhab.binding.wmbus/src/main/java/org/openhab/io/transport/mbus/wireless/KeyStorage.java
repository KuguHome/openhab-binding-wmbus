/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.mbus.wireless;

import java.util.Map;
import java.util.Optional;

import org.openmuc.jmbus.SecondaryAddress;

/**
 * A simple abstraction over wmbus transport layer which allows to work with message encryption keys without knowing
 * anything at all about jmbus library, its dependencies and wmbus in general.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public interface KeyStorage {

    Optional<byte[]> lookupKey(byte[] address);

    void registerKey(byte[] address, byte[] key);

    Map<SecondaryAddress, byte[]> toMap();

}
