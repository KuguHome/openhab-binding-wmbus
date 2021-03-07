/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
