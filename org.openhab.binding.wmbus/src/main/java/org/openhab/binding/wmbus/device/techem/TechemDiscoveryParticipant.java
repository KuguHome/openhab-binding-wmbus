/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.device.AbstractWMBusDiscoveryParticipant;
import org.openhab.binding.wmbus.discovery.WMBusDiscoveryParticipant;
import org.openhab.binding.wmbus.internal.TechemHKV;
import org.openhab.binding.wmbus.internal.WMBusDevice;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Discovers techem devices and decodes records which are broadcasted by it.
 *
 * @author Łukasz Dywicki - extraction of logic from compound discovery service.
 */
@Component(immediate = true)
public class TechemDiscoveryParticipant extends AbstractWMBusDiscoveryParticipant implements WMBusDiscoveryParticipant {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(WMBusBindingConstants.THING_TYPE_TECHEM_HKV);

    private static final Map<String, String> SUPPORTED_DEVICE_VARIANTS = ImmutableMap.<String, String> builder()
            .put("68TCH97255", WMBusBindingConstants.THING_TYPE_NAME_TECHEM_HKV) // unsure,
            .put("68TCH105255", WMBusBindingConstants.THING_TYPE_NAME_TECHEM_HKV)
            .put("68TCH116255", WMBusBindingConstants.THING_TYPE_NAME_TECHEM_HKV) // unsure,
            .put("68TCH118255", WMBusBindingConstants.THING_TYPE_NAME_TECHEM_HKV) // find out, if they work
            .build();

    private final Logger logger = LoggerFactory.getLogger(TechemDiscoveryParticipant.class);

    @Override
    public @NonNull Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public @Nullable ThingUID getThingUID(WMBusDevice device) {
        WMBusMessage message = device.getOriginalMessage();

        if (!"TCH".equals(message.getSecondaryAddress().getManufacturerId())
                || SUPPORTED_DEVICE_VARIANTS.containsKey(device.getDeviceType())) {
            return null;
        }

        WMBusDevice techemDevice = new TechemHKV(message, device.getAdapter());
        try {
            logger.trace("Attempt to decode as Techem message");
            techemDevice.decode();

            return super.getThingUID(techemDevice);
        } catch (DecodingException e) {
            logger.debug("Could not decode message '{}', not a techem device", message, e);
        }

        return null;
    }

    @Override
    public DiscoveryResult createResult(WMBusDevice device) {
        ThingUID thingUID = getThingUID(device);

        if (thingUID != null) {
            String label = "Heat cost allocator #" + device.getDeviceId() + " (" + device.getDeviceType() + ")";

            Map<String, Object> properties = new HashMap<>();
            properties.put(WMBusBindingConstants.PROPERTY_DEVICE_ID, device.getDeviceId());
            properties.put(WMBusBindingConstants.PROPERTY_DEVICE_ADDRESS, device.getDeviceAddress());
            properties.put(Thing.PROPERTY_VENDOR, "Techem");
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getDeviceId());
            SecondaryAddress secondaryAddress = device.getOriginalMessage().getSecondaryAddress();
            properties.put(Thing.PROPERTY_MODEL_ID, secondaryAddress.getVersion());

            // Create the discovery result and add to the inbox
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(WMBusBindingConstants.PROPERTY_DEVICE_ID).withLabel(label)
                    .withThingType(WMBusBindingConstants.THING_TYPE_TECHEM_HKV).withBridge(device.getAdapter().getUID())
                    .withLabel(label).build();
        }

        return null;
    }

    @Override
    protected ThingTypeUID getThingType() {
        return WMBusBindingConstants.THING_TYPE_TECHEM_HKV;
    }

}
