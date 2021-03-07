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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.core.thing.Thing;
import org.openhab.core.util.HexUtils;
import org.openmuc.jmbus.SecondaryAddress;

/**
 * Filtering implementation of {@link KeyStorage} - permits operation on single address and no other.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class FilteredKeyStorage implements KeyStorage {

    private final KeyStorage delegate;
    private final byte[] address;

    public FilteredKeyStorage(KeyStorage delegate, Thing thing) {
        this.delegate = delegate;
        this.address = Optional.ofNullable(thing.getConfiguration())
                .map(cfg -> cfg.get(WMBusBindingConstants.PROPERTY_DEVICE_ADDRESS)).map(Object::toString)
                .map(HexUtils::hexToBytes).orElse(new byte[0]);
    }

    @Override
    public Optional<byte[]> lookupKey(byte[] address) {
        if (Arrays.equals(this.address, address)) {
            return delegate.lookupKey(address);
        }
        return Optional.empty();
    }

    @Override
    public void registerKey(byte[] address, byte[] key) {
        if (Arrays.equals(this.address, address)) {
            delegate.registerKey(address, key);
        }
    }

    @Override
    public Map<SecondaryAddress, byte[]> toMap() {
        return lookupKey(address).map(key -> Collections.singletonMap(createKey(address), key))
                .orElse(Collections.emptyMap());
    }

    private SecondaryAddress createKey(byte[] address) {
        return SecondaryAddress.newFromWMBusLlHeader(address, 0);
    }
}
