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

import javax.measure.Unit;

import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.wmbus.internal.RecordType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import tec.uom.se.unit.Units;

/**
 * Subset of WMBus constants specific to Fastforward/Q-Loud devices.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public interface QloudBindingConstants {

    // record fields reported by energycam
    RecordType VOLUME_CUBIC_METRE = new RecordType(0x04, 0x15);
    RecordType ELECTRICITY_WATT_HOUR = new RecordType(0x04, 0x05);

    String BINDING_ID = "qloud";

    String THING_TYPE_NAME_ENERGYCAM_OIL = "energycam_oil";
    String THING_TYPE_NAME_ENERGYCAM_ELECTRICITY = "energycam_electricity";
    String THING_TYPE_NAME_ENERGYCAM_GAS = "energycam_gas";
    String THING_TYPE_NAME_ENERGYCAM_WATER = "energycam_water";

    ThingTypeUID THING_TYPE_ENERGYCAM_OIL = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_ENERGYCAM_OIL);
    ThingTypeUID THING_TYPE_ENERGYCAM_ELECTRICITY = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_ENERGYCAM_ELECTRICITY);
    ThingTypeUID THING_TYPE_ENERGYCAM_GAS = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_ENERGYCAM_GAS);
    ThingTypeUID THING_TYPE_ENERGYCAM_WATER = new ThingTypeUID(BINDING_ID, THING_TYPE_NAME_ENERGYCAM_WATER);

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
            THING_TYPE_ENERGYCAM_WATER, ImmutableMap.of(CHANNEL_VOLUME, new RecordType(0, 0x15)));

    Map<String, ThingTypeUID> SUPPORTED_DEVICE_VARIANTS = ImmutableMap.<String, ThingTypeUID> builder()
            // here we enlist supported devices
            .put("68FFD11", QloudBindingConstants.THING_TYPE_ENERGYCAM_OIL)
            .put("68FFD12", QloudBindingConstants.THING_TYPE_ENERGYCAM_ELECTRICITY)
            .put("68FFD13", QloudBindingConstants.THING_TYPE_ENERGYCAM_GAS)
            .put("68FFD17", QloudBindingConstants.THING_TYPE_ENERGYCAM_WATER).build();

    Map<RecordType, Unit<?>> RECORD_UNITS = ImmutableMap.of(VOLUME_CUBIC_METRE, Units.CUBIC_METRE,
            ELECTRICITY_WATT_HOUR, SmartHomeUnits.WATT_HOUR);

    Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.copyOf(SUPPORTED_DEVICE_VARIANTS.values());

}
