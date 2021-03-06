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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.openmuc.jmbus.SecondaryAddress;
import org.osgi.service.component.annotations.Component;

/**
 * Simplistic implementation of {@link KeyStorage} backed by Map.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
@Component
public class MapKeyStorage implements KeyStorage {

    private final Map<SecondaryAddress, byte[]> keyMap = new ConcurrentHashMap<>();

    @Override
    public Optional<byte[]> lookupKey(byte[] address) {
        return Optional.ofNullable(keyMap.get(createKey(address)));
    }

    @Override
    public void registerKey(byte[] address, byte[] key) {
        keyMap.put(createKey(address), key);
    }

    @Override
    public Map<SecondaryAddress, byte[]> toMap() {
        return Collections.unmodifiableMap(keyMap);
    }

    private SecondaryAddress createKey(byte[] address) {
        return SecondaryAddress.newFromWMBusHeader(address, 0);
    }
}
