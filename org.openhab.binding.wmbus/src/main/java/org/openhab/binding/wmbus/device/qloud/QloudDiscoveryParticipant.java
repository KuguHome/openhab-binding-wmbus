/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.qloud;

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
import org.openhab.binding.wmbus.BindingConfiguration;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.AbstractWMBusDiscoveryParticipant;
import org.openhab.binding.wmbus.discovery.WMBusDiscoveryParticipant;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers fastforward energy cam devices.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
@Component(immediate = true)
public class QloudDiscoveryParticipant extends AbstractWMBusDiscoveryParticipant implements WMBusDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(QloudDiscoveryParticipant.class);

    @Override
    public @NonNull Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return QloudBindingConstants.SUPPORTED_THING_TYPES;
    }

    @Override
    public @Nullable ThingUID getThingUID(WMBusDevice device) {
        WMBusMessage message = device.getOriginalMessage();

        // Q-loud doesn't have its own manufacturer
        if (!QloudBindingConstants.MANUFACTURER_ID.equals(message.getSecondaryAddress().getManufacturerId())) {
            return null;
        }

        if (!QloudBindingConstants.SUPPORTED_DEVICE_VARIANTS.containsKey(device.getDeviceType())) {
            logger.trace("Found unsupported Q-lound/Fastforwar device {}, ommiting it from discovery results.",
                    device.getDeviceType());
            return null;
        }

        try {
            WMBusDevice qloudDevice = new QloudWMBusDevice(message, device.getAdapter());
            logger.trace("Attempt to decode as message with CRC fields");
            qloudDevice.decode();

            return super.getThingUID(qloudDevice);
        } catch (DecodingException e) {
            logger.debug("Could not decode message '{}', not a qloud device", message, e);
        }

        return null;
    }

    @Override
    public DiscoveryResult createResult(WMBusDevice device) {
        ThingUID thingUID = getThingUID(device);

        if (thingUID != null) {
            SecondaryAddress secondaryAddress = device.getOriginalMessage().getSecondaryAddress();

            String label = "EnergyCam " + getMedium(secondaryAddress) + " #" + device.getDeviceId() + " ("
                    + device.getDeviceType() + ")";

            Map<String, Object> properties = new HashMap<>();
            properties.put(WMBusBindingConstants.PROPERTY_DEVICE_ADDRESS, device.getDeviceAddress());
            properties.put(Thing.PROPERTY_VENDOR, "Q-loud GmbH");
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getDeviceId());
            properties.put(Thing.PROPERTY_MODEL_ID, secondaryAddress.getVersion());

            // Create the discovery result and add to the inbox
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(WMBusBindingConstants.PROPERTY_DEVICE_ADDRESS).withLabel(label)
                    .withThingType(QloudBindingConstants.SUPPORTED_DEVICE_VARIANTS.get(device.getDeviceType()))
                    .withBridge(device.getAdapter().getUID()).withLabel(label).withTTL(getTimeToLive()).build();
        }

        return null;
    }

    private String getMedium(SecondaryAddress secondaryAddress) {
        return secondaryAddress.getDeviceType().name().toLowerCase().replace("_", " ");
    }

    @Override
    protected ThingTypeUID getThingType(WMBusDevice device) {
        switch (device.getOriginalMessage().getSecondaryAddress().getDeviceType()) {
            case OIL_METER:
                return QloudBindingConstants.THING_TYPE_ENERGYCAM_OIL;
            case ELECTRICITY_METER:
                return QloudBindingConstants.THING_TYPE_ENERGYCAM_ELECTRICITY;
            case GAS_METER:
                return QloudBindingConstants.THING_TYPE_ENERGYCAM_GAS;
            case WATER_METER:
                return QloudBindingConstants.THING_TYPE_ENERGYCAM_WATER;
        }

        return null;
    }

    @Override
    @Reference
    public void setBindingConfiguration(BindingConfiguration configuration) {
        super.setBindingConfiguration(configuration);
    }

    @Override
    public void unsetBindingConfiguration(BindingConfiguration configuration) {
        super.unsetBindingConfiguration(configuration);
    }

}
