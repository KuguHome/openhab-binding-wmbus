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
package org.openhab.binding.wmbus.device.techem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wmbus.BindingConfiguration;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.decoder.TechemFrameDecoder;
import org.openhab.binding.wmbus.discovery.WMBusDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
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

    private BindingConfiguration configuration;

    @Override
    public @NonNull Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return TechemBindingConstants.SUPPORTED_THING_TYPES;
    }

    @Override
    public @Nullable ThingUID getThingUID(WMBusDevice device) {
        if (configuration.getIncludeBridgeUID()) {
            return decodeDevice(device).map(this::getThingType)
                    .map(type -> new ThingUID(type, device.getAdapter().getUID(), device.getDeviceId())).orElse(null);
        } else {
            return decodeDevice(device).map(this::getThingType).map(type -> new ThingUID(type, device.getDeviceId()))
                    .orElse(null);
        }
    }

    protected @Nullable ThingUID getThingUID(TechemDevice device) {
        if (configuration.getIncludeBridgeUID()) {
            return Optional.ofNullable(getThingType(device))
                    .map(type -> new ThingUID(type, device.getAdapter().getUID(), device.getDeviceId())).orElse(null);
        } else {
            return Optional.ofNullable(getThingType(device)).map(type -> new ThingUID(type, device.getDeviceId()))
                    .orElse(null);
        }
    }

    @Override
    public DiscoveryResult createResult(WMBusDevice device) {
        Optional<TechemDevice> decodeDevice = decodeDevice(device);
        ThingUID thingUID = decodeDevice.map(this::getThingUID).orElse(null);

        if (thingUID != null) {
            String deviceTypeLabel = decodeDevice.map(TechemDevice::getTechemDeviceType)
                    .map(WMBusBindingConstants.DEVICE_TYPE_TRANSFORMATION).orElse("Unknown");
            Variant deviceTypeTag = decodeDevice.map(TechemDevice::getDeviceVariant).orElseThrow(
                    () -> new IllegalArgumentException("Unmapped techem device " + device.getDeviceType()));

            String label = "Techem " + deviceTypeLabel + " #" + device.getDeviceId() + " ("
                    + deviceTypeTag.getTechemType() + ")";

            Map<String, Object> properties = new HashMap<>();
            properties.put(WMBusBindingConstants.PROPERTY_DEVICE_ADDRESS, device.getDeviceAddress());
            properties.put(Thing.PROPERTY_VENDOR, "Techem");
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getDeviceId());
            SecondaryAddress secondaryAddress = device.getOriginalMessage().getSecondaryAddress();
            properties.put(Thing.PROPERTY_MODEL_ID, secondaryAddress.getVersion());

            // Create the discovery result and add to the inbox
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(WMBusBindingConstants.PROPERTY_DEVICE_ADDRESS).withLabel(label)
                    .withThingType(TechemBindingConstants.SUPPORTED_DEVICE_VARIANTS.get(deviceTypeTag))
                    .withBridge(device.getAdapter().getUID()).withLabel(label).withTTL(getTimeToLive()).build();
        }

        return null;
    }

    private final Optional<TechemDevice> decodeDevice(WMBusDevice device) {
        WMBusMessage message = device.getOriginalMessage();

        if (!"TCH".equals(message.getSecondaryAddress().getManufacturerId())) {
            return Optional.empty();
        }

        if (!TechemBindingConstants.SUPPORTED_DEVICE_TYPES.contains(device.getRawDeviceType())) {
            logger.trace("Found unsupported Techem device {}, omitting it from discovery results.",
                    device.getDeviceType());
            return Optional.empty();
        }

        logger.trace("Attempt to decode received Techem telegram");
        return Optional.ofNullable(techemFrameDecoder.decode(device));
    }

    private @Nullable ThingTypeUID getThingType(TechemDevice device) {
        return TechemBindingConstants.SUPPORTED_DEVICE_VARIANTS.get(device.getDeviceVariant());
    }

    @Reference
    public void setTechemFrameDecoder(TechemFrameDecoder<TechemDevice> techemFrameDecoder) {
        this.techemFrameDecoder = techemFrameDecoder;
    }

    public void unsetTechemFrameDecoder(TechemFrameDecoder<TechemDevice> techemFrameDecoder) {
        this.techemFrameDecoder = null;
    }

    @Reference
    public void setBindingConfiguration(BindingConfiguration configuration) {
        this.configuration = configuration;
    }

    public void unsetBindingConfiguration(BindingConfiguration configuration) {
        this.configuration = null;
    }

    private Long getTimeToLive() {
        return configuration.getTimeToLive();
    }
}
