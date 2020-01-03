/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        return SecondaryAddress.newFromWMBusLlHeader(address, 0);
    }

}
