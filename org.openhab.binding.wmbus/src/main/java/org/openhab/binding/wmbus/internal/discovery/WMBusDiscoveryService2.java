/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wmbus.BindingConfiguration;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusCompanyIdentifiers;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.discovery.WMBusDiscoveryParticipant;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.handler.WMBusMessageListener;
import org.openmuc.jmbus.SecondaryAddress;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WMBusDiscoveryService2} handles searching for WMBus devices.
 *
 * @author Łukasz Dywicki - initial Contribution
 */
@Component(immediate = true, service = { DiscoveryService.class,
        WMBusMessageListener.class }, configurationPid = "discovery.wmbus")
public class WMBusDiscoveryService2 extends AbstractDiscoveryService implements WMBusMessageListener {

    private final Logger logger = LoggerFactory.getLogger(WMBusDiscoveryService2.class);

    private static final int SEARCH_TIME = 0;

    private final Set<WMBusDiscoveryParticipant> participants = new CopyOnWriteArraySet<>();
    private final Set<ThingTypeUID> supportedThingTypes = new CopyOnWriteArraySet<>();

    private BindingConfiguration configuration;

    public WMBusDiscoveryService2() {
        super(SEARCH_TIME);
        supportedThingTypes.add(WMBusBindingConstants.THING_TYPE_METER);
    }

    @Override
    public boolean isBackgroundDiscoveryEnabled() {
        return true;
    }

    @Override
    @Activate
    protected void activate(Map<String, Object> configProperties) {
        logger.debug("Activating WMBus discovery service");
        super.activate(configProperties);
        startScan();
    }

    @Override
    @Modified
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating WMBus discovery service");
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addWMBusDiscoveryParticipant(WMBusDiscoveryParticipant participant) {
        this.participants.add(participant);
        supportedThingTypes.addAll(participant.getSupportedThingTypeUIDs());
    }

    protected void removeWMBusDiscoveryParticipant(WMBusDiscoveryParticipant participant) {
        supportedThingTypes.removeAll(participant.getSupportedThingTypeUIDs());
        this.participants.remove(participant);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return supportedThingTypes;
    }

    @Override
    public void startScan() {
    }

    @Override
    public void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void onNewWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {
        deviceDiscovered(adapter, device);
    }

    @Override
    public void onChangedWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {

    }

    private void deviceDiscovered(WMBusAdapter adapter, WMBusDevice device) {
        for (WMBusDiscoveryParticipant participant : participants) {
            try {
                DiscoveryResult result = participant.createResult(device);
                if (result != null) {
                    thingDiscovered(result);
                    return;
                }
            } catch (Exception e) {
                logger.error("Participant '{}' threw an exception.", participant.getClass().getName(), e);
            }
        }

        // Here we have an additional section which discard devices of unknown type.
        // Some of standard WMBus devices are not meant to be read by this binding.
        // If earlier discovery participants didn't handle properly device we need to narrow scope to meters,
        // valves and other standard accessory excluding repeaters and other special things.
        SecondaryAddress secondaryAddress = device.getOriginalMessage().getSecondaryAddress();
        if (/*
             * !WMBusBindingConstants.SUPPORTED_DEVICE_TYPES.contains(secondaryAddress.getDeviceType())
             * ||
             */ !device.getDeviceId().matches("^[a-zA-Z0-9]+$")) {
            logger.info("Discarded discovery of device {} which is unsupported by binding: {}", device.getDeviceType(),
                    secondaryAddress);
            return;
        }

        // We did not find a thing type for this device, so let's treat it as a generic one
        String label = "WMBus device: " + secondaryAddress.getDeviceType().name().toLowerCase().replace("_", " ") + " #"
                + device.getDeviceType();

        Map<String, Object> properties = new HashMap<>();
        properties.put(WMBusBindingConstants.PROPERTY_DEVICE_ID, device.getDeviceId());
        properties.put(WMBusBindingConstants.PROPERTY_DEVICE_ADDRESS, device.getDeviceAddress());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getDeviceId());
        properties.put(Thing.PROPERTY_MODEL_ID, secondaryAddress.getVersion());

        String manufacturer = WMBusCompanyIdentifiers.get(secondaryAddress.getManufacturerId());
        if (manufacturer != null) {
            properties.put(Thing.PROPERTY_VENDOR, manufacturer);
            label += " (" + manufacturer + ")";
        }

        ThingTypeUID typeUID = WMBusBindingConstants.THING_TYPE_METER;
        ThingUID thingUID = new ThingUID(typeUID, device.getDeviceId());

        // Create the discovery result and add to the inbox
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(WMBusBindingConstants.PROPERTY_DEVICE_ID).withBridge(adapter.getUID())
                .withThingType(typeUID).withLabel(label).withTTL(getTimeToLive()).build();

        thingDiscovered(discoveryResult);
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
