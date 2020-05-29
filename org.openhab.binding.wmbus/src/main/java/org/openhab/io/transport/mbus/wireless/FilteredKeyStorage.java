/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.mbus.wireless;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openmuc.jmbus.SecondaryAddress;

/**
 * Filtering implementation of {@link KeyStorage} - permits operation on single address and no other.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class FilteredKeyStorage implements KeyStorage {

    private final KeyStorage delegate;
    private final byte[] address;
    private final byte[] meterAddress;

    public FilteredKeyStorage(KeyStorage delegate, Thing thing) {
        this.delegate = delegate;
        this.address = Optional.ofNullable(thing.getConfiguration())
                .map(cfg -> cfg.get(WMBusBindingConstants.PROPERTY_DEVICE_ADDRESS)).map(Object::toString)
                .map(HexUtils::hexToBytes).orElse(new byte[0]);
        this.meterAddress = Optional.ofNullable(thing.getConfiguration())
                .map(cfg -> cfg.get(WMBusBindingConstants.PROPERTY_METER_ADDRESS)).map(Object::toString)
                .map(HexUtils::hexToBytes).orElse(new byte[0]);
    }

    @Override
    public Optional<byte[]> lookupKey(byte[] address) {
        if (Arrays.equals(this.address, address) || Arrays.equals(this.meterAddress, address)) {
            return delegate.lookupKey(address);
        }
        return Optional.empty();
    }

    @Override
    public void registerKey(byte[] address, byte[] key) {
        if (Arrays.equals(this.address, address) || Arrays.equals(this.meterAddress, address)) {
            delegate.registerKey(address, key);
        }
    }

    @Override
    public Map<SecondaryAddress, byte[]> toMap() {
        Map<SecondaryAddress, byte[]> addressKeys = lookupKey(address)
                .map(key -> Collections.singletonMap(createKey(address), key)).orElse(Collections.emptyMap());
        Map<SecondaryAddress, byte[]> meterAddressKey = lookupKey(meterAddress)
                .map(key -> Collections.singletonMap(createKey(meterAddress), key)).orElse(Collections.emptyMap());

        addressKeys.putAll(meterAddressKey);
        return addressKeys;
    }

    private SecondaryAddress createKey(byte[] address) {
        return SecondaryAddress.newFromWMBusHeader(address, 0);
    }

}
