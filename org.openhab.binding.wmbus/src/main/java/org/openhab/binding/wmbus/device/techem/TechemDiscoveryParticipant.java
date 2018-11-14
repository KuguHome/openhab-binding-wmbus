/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.decoder.TechemFrameDecoder;
import org.openhab.binding.wmbus.discovery.WMBusDiscoveryParticipant;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers techem devices and decodes records which are broadcasted by it.
 *
 * @author Łukasz Dywicki - extraction of logic from compound discovery service.
 */
@Component
public class TechemDiscoveryParticipant implements WMBusDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(TechemDiscoveryParticipant.class);

    private TechemFrameDecoder<TechemDevice> techemFrameDecoder;

    @Override
    public @NonNull Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return TechemBindingConstants.SUPPORTED_THING_TYPES;
    }

    @Override
    public @Nullable ThingUID getThingUID(WMBusDevice device) {
        return decodeDevice(device).map(TechemDevice::getDeviceType)
                .map(TechemBindingConstants.SUPPORTED_DEVICE_VARIANTS::get)
                .map(type -> new ThingUID(type, device.getDeviceId())).orElse(null);

    }

    @Override
    public DiscoveryResult createResult(WMBusDevice device) {
        Optional<TechemDevice> decodeDevice = decodeDevice(device);
        ThingUID thingUID = decodeDevice.map(this::getThingUID).orElse(null);

        if (thingUID != null) {
            String deviceTypeLabel = decodeDevice.map(TechemDevice::getTechemDeviceType)
                    .map(WMBusBindingConstants.DEVICE_TYPE_TRANSFORMATION).orElse("Unknown");
            String deviceTypeTag = decodeDevice.map(TechemDevice::getDeviceType)
                    .orElseGet(() -> device.getDeviceType());

            String label = "Techem " + deviceTypeLabel + " #" + device.getDeviceId() + " (" + deviceTypeTag + ")";

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
                    .withThingType(TechemBindingConstants.SUPPORTED_DEVICE_VARIANTS.get(device.getDeviceType()))
                    .withBridge(device.getAdapter().getUID()).withLabel(label).build();
        }

        return null;
    }

    private final Optional<TechemDevice> decodeDevice(WMBusDevice device) {
        WMBusMessage message = device.getOriginalMessage();

        if (!"TCH".equals(message.getSecondaryAddress().getManufacturerId())) {
            return Optional.empty();
        }

        if (!TechemBindingConstants.SUPPORTED_DEVICE_VARIANTS.containsKey(device.getDeviceType())) {
            logger.trace("Found unsupported Techem device {}, ommiting it from discovery results.",
                    device.getDeviceType());
        }

        logger.trace("Attempt to decode received Techem telegram");
        return Optional.ofNullable(techemFrameDecoder.decode(device));
    }

    @Reference
    public void setTechemFrameDecoder(TechemFrameDecoder<TechemDevice> techemFrameDecoder) {
        this.techemFrameDecoder = techemFrameDecoder;
    }

    public void unsetTechemFrameDecoder(TechemFrameDecoder<TechemDevice> techemFrameDecoder) {
        this.techemFrameDecoder = null;
    }

}
