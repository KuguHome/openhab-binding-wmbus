/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.internal.units;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openmuc.jmbus.DlmsUnit;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * An unit registry intended to aggregate several other registers to mask actual lookup operation.
 *
 * By default this registry is started up with {@link SmartHomeUnitsRegistry} which covers standard DLMS-SI/Imperial
 * units known to
 * framework. However some DLMS units are not supported and very specific to narrow fields which might not be added any
 * time soon. For this reason we leave an extensions for future cases if there is a device we desperately want, but its
 * dlms measurements units are not supported.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
@Component(property = { "composite=true" })
public class CompositeUnitRegistry implements UnitRegistry {

    private final Set<UnitRegistry> registers = new LinkedHashSet<>();

    public CompositeUnitRegistry() {
        this(new SmartHomeUnitsRegistry());
    }

    CompositeUnitRegistry(UnitRegistry... registers) {
        this(Arrays.asList(registers));
    }

    CompositeUnitRegistry(Collection<UnitRegistry> initial) {
        this.registers.addAll(initial);
    }

    @Override
    public Optional<Unit<?>> lookup(DlmsUnit wmbusType) {
        return registers.stream() //
                .flatMap(registry -> get(registry, wmbusType)) //
                .findFirst();
    }

    @Override
    public Optional<Class<? extends Quantity<?>>> quantity(@Nullable DlmsUnit wmbusType) {
        return registers.stream() //
                .flatMap(registry -> getQuantity(registry, wmbusType)) //
                .findFirst();
    }

    private Stream<Unit<?>> get(UnitRegistry registry, DlmsUnit wmbusType) {
        Optional<Unit<?>> lookup = registry.lookup(wmbusType);

        if (lookup.isPresent()) {
            return Stream.of(lookup.get());
        }
        return Stream.empty();
    }

    private Stream<Class<? extends Quantity<?>>> getQuantity(UnitRegistry registry, @Nullable DlmsUnit wmbusType) {
        Optional<Class<? extends Quantity<?>>> lookup = registry.quantity(wmbusType);

        if (lookup.isPresent()) {
            return Stream.of(lookup.get());
        }
        return Stream.empty();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, target = "(!(composite=true))")
    protected void setUnitRegistry(UnitRegistry registry) {
        this.registers.add(registry);
    }

    protected void unsetUnitRegistry(UnitRegistry registry) {
        this.registers.remove(registry);
    }

}
