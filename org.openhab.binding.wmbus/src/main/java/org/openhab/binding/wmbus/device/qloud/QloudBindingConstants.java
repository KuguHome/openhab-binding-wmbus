/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.qloud;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.WMBusBindingConstants;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Subset of WMBus constants specific to Fastforward/Q-Loud devices.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public interface QloudBindingConstants {

    String MANUFACTURER_ID = "FFD";

    // record fields reported by energycam
    RecordType VOLUME_CUBIC_METRE = new RecordType(0x04, 0x15);
    RecordType ELECTRICITY_WATT_HOUR = new RecordType(0x04, 0x05);

    String THING_TYPE_NAME_ENERGYCAM_OIL = "energycam_oil";
    String THING_TYPE_NAME_ENERGYCAM_ELECTRICITY = "energycam_electricity";
    String THING_TYPE_NAME_ENERGYCAM_GAS = "energycam_gas";
    String THING_TYPE_NAME_ENERGYCAM_WATER = "energycam_water";

    ThingTypeUID THING_TYPE_ENERGYCAM_OIL = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_ENERGYCAM_OIL);
    ThingTypeUID THING_TYPE_ENERGYCAM_ELECTRICITY = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_ENERGYCAM_ELECTRICITY);
    ThingTypeUID THING_TYPE_ENERGYCAM_GAS = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_ENERGYCAM_GAS);
    ThingTypeUID THING_TYPE_ENERGYCAM_WATER = new ThingTypeUID(WMBusBindingConstants.BINDING_ID,
            THING_TYPE_NAME_ENERGYCAM_WATER);

    String CHANNEL_ENERGY = "energy";
    String CHANNEL_VOLUME = "volume";

    Map<ThingTypeUID, Map<String, RecordType>> RECORD_MAP = ImmutableMap.of(
            // we have only volume
            THING_TYPE_ENERGYCAM_OIL, ImmutableMap.of(CHANNEL_VOLUME, VOLUME_CUBIC_METRE),
            // total energy
            THING_TYPE_ENERGYCAM_ELECTRICITY, ImmutableMap.of(CHANNEL_ENERGY, ELECTRICITY_WATT_HOUR),
            // again volume
            THING_TYPE_ENERGYCAM_GAS, ImmutableMap.of(CHANNEL_VOLUME, VOLUME_CUBIC_METRE),
            // volume one more time
            THING_TYPE_ENERGYCAM_WATER, ImmutableMap.of(CHANNEL_VOLUME, VOLUME_CUBIC_METRE));

    Map<String, ThingTypeUID> SUPPORTED_DEVICE_VARIANTS = ImmutableMap.<String, ThingTypeUID> builder()
            // here we enlist supported devices
            .put("68FFD11", QloudBindingConstants.THING_TYPE_ENERGYCAM_OIL)
            .put("68FFD12", QloudBindingConstants.THING_TYPE_ENERGYCAM_ELECTRICITY)
            .put("68FFD13", QloudBindingConstants.THING_TYPE_ENERGYCAM_GAS)
            .put("68FFD17", QloudBindingConstants.THING_TYPE_ENERGYCAM_WATER).build();

    Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.copyOf(SUPPORTED_DEVICE_VARIANTS.values());

}
