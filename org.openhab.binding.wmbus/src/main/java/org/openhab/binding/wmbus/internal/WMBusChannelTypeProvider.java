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
package org.openhab.binding.wmbus.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.config.DateFieldMode;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.handler.WMBusMessageListener;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.EventDescription;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.util.HexUtils;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DataRecord.DataValueType;
import org.openmuc.jmbus.DataRecord.Description;
import org.openmuc.jmbus.DataRecord.FunctionField;
import org.openmuc.jmbus.DlmsUnit;
import org.openmuc.jmbus.VariableDataStructure;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic channel type provider which uses received wmbus frames to create channel types.
 *
 * Because wmbus frames contain variable data part which have many mutations it is not possible to create a static
 * configuration which would cover all combinations. Fields which are multipliers are:
 * - dib (inst/min/max/error val)
 * - vib (subunit, tariff, storage number, vif)
 *
 * While most of devices uses just small subset of values its not possible to predict all variations of above.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
@Component(service = { ChannelTypeProvider.class, WMBusMessageListener.class, WMBusChannelTypeProvider.class })
public class WMBusChannelTypeProvider implements ChannelTypeProvider, WMBusMessageListener {

    private final Logger logger = LoggerFactory.getLogger(WMBusChannelTypeProvider.class);

    private final Map<String, ChannelType> wmbusChannelMap = new ConcurrentHashMap<>();
    private UnitRegistry unitRegistry;

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return wmbusChannelMap.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return wmbusChannelMap.values().stream().filter(channelType -> channelType.getUID().equals(channelTypeUID))
                .findFirst().orElse(null);
    }

    @Override
    public void onNewWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {
        calculateChannelTypes(device);
    }

    @Override
    public void onChangedWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {
        calculateChannelTypes(device);
    }

    private void calculateChannelTypes(WMBusDevice device) {
        VariableDataStructure response = device.getOriginalMessage().getVariableDataResponse();
        DateFieldMode dateFieldMode = device.getAdapter().getDateFieldMode();

        for (DataRecord record : response.getDataRecords()) {
            Optional<ChannelTypeUID> channelTypeUID = getChannelType(record);
            if (channelTypeUID.isPresent()) {
                ChannelTypeUID typeUID = channelTypeUID.get();
                if (!wmbusChannelMap.containsKey(typeUID.getId())) {
                    Optional<Unit<?>> unit = unitRegistry.lookup(record.getUnit());
                    String label = getFunction(record.getFunctionField()) + " ";
                    label += record.getDescription().name().toLowerCase().replace("_", " ");
                    if (record.getTariff() != 0) {
                        label += " tariff " + record.getTariff();
                    }
                    if (record.getStorageNumber() != 0) {
                        label += " storage " + record.getStorageNumber();
                    }

                    logger.info("Calculating new channel type {} for record {}", channelTypeUID, record);

                    Optional<String> itemType = getItemType(record.getDataValueType(), record.getUnit(), dateFieldMode);
                    ChannelKind kind = ChannelKind.STATE;
                    String description = getDescription(record);
                    String category = "";
                    Set<String> tags = Collections.emptySet();
                    StateDescription state = getStateDescription(record.getDataValueType(), record.getDescription(),
                            unit, dateFieldMode);
                    EventDescription event = null;
                    ChannelTypeBuilder channelTypeBuilder;
                    ChannelType channelType = ChannelTypeBuilder.state(channelTypeUID.get(), label, itemType.get())
                            .isAdvanced(false).withDescription(description).withCategory(category)
                            .build();/*
                                      * new ChannelType(typeUID, false, itemType.get(), kind, label,
                                      * description,
                                      * category, tags, state, null, event, null, null)
                                      */

                    wmbusChannelMap.put(typeUID.getId(), channelType);
                }
            }
        }
    }

    private StateDescription getStateDescription(DataValueType type, Description description,
            Optional<Unit<?>> mappedUnit, DateFieldMode dateFieldMode) {

        boolean number;
        if (type == DataValueType.BCD || type == DataValueType.DOUBLE || type == DataValueType.LONG) {
            number = true;
        } else {
            number = false;
        }

        boolean date = type == DataValueType.DATE;

        String pattern = mappedUnit.map(unit -> formatUnit(false, number, date, dateFieldMode))
                .orElseGet(() -> formatUnit(true, number, date, dateFieldMode));
        StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create().withPattern(pattern)
                .withReadOnly(true);
        return stateFragment.build()
                .toStateDescription()/* new StateDescription(null, null, null, pattern, true, null) */;
    }

    private String formatUnit(boolean unitless, boolean number, boolean date, DateFieldMode dateFieldMode) {
        if (number) {
            if (unitless) {
                return "%.2f";
            } else {
                return "%.2f %unit%";
            }
        }

        if (date) {
            switch (dateFieldMode) {
                case UNIX_TIMESTAMP:
                    return "%d";
                case DATE_TIME:
                    return "";
                // default in this case is string
            }
        }

        return "%s";
    }

    private String getDescription(DataRecord record) {
        String description = record.getDescription().name().replace("_", " ").toLowerCase();
        String function = getFunction(record.getFunctionField());
        return function + " value of " + description + " registry. Storage " + record.getStorageNumber() + ", tarif "
                + record.getTariff() + ". Emmited under DIB " + HexUtils.bytesToHex(record.getDib()) + " and VIB:"
                + HexUtils.bytesToHex(record.getVib());
    }

    private String getFunction(FunctionField function) {
        switch (function) {
            case ERROR_VAL:
                return "Error";
            case INST_VAL:
                return "Present";
            case MAX_VAL:
                return "Maximum";
            case MIN_VAL:
                return "Minimum";

        }

        return "Unknown";
    }

    private Optional<String> getItemType(DataValueType dataValueType, DlmsUnit dlmsUnit, DateFieldMode dateFieldMode) {
        switch (dataValueType) {
            case BCD:
            case DOUBLE:
            case LONG:
                String quantity = unitRegistry.quantity(dlmsUnit).map(Class::getSimpleName)
                        .map(quantityName -> ":" + quantityName).orElse("");
                return Optional.of(CoreItemFactory.NUMBER + quantity);
            case DATE:
                switch (dateFieldMode) {
                    case FORMATTED_STRING:
                        return Optional.of(CoreItemFactory.STRING);
                    case UNIX_TIMESTAMP:
                        return Optional.of(CoreItemFactory.NUMBER);
                    default:
                        return Optional.of(CoreItemFactory.DATETIME);
                }
            case STRING:
                return Optional.of(CoreItemFactory.STRING);
        }

        return Optional.of(CoreItemFactory.STRING);
    }

    @Reference
    protected void setUnitRegistry(UnitRegistry unitRegistry) {
        this.unitRegistry = unitRegistry;
    }

    protected void unsetUnitRegistry(UnitRegistry unitRegistry) {
        this.unitRegistry = null;
    }

    public final static Optional<String> getChannelId(DataRecord record) {
        if (record.getDescription() == Description.RESERVED || record.getDescription() == Description.NOT_SUPPORTED
                || record.getDescription() == Description.MANUFACTURER_SPECIFIC) {
            return Optional.empty();
        }

        String dib = HexUtils.bytesToHex(record.getDib());
        String vib = HexUtils.bytesToHex(record.getVib());

        return Optional.of(record.getDescription().name().toLowerCase() + "_" + dib + "_" + vib);
    }

    public final static Optional<ChannelTypeUID> getChannelType(DataRecord record) {
        return getChannelId(record).map(id -> new ChannelTypeUID(WMBusBindingConstants.BINDING_ID, id));
    }
}
