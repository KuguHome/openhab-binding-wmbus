/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.internal.units;

import java.util.Optional;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.assertj.core.api.Assertions;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.core.library.unit.Units;
import org.openmuc.jmbus.DlmsUnit;

/**
 * Test of {@link CompositeUnitRegistry} with {@link UnitsRegistry} and specific extension which provides
 * support for {@link DlmsUnit#COUNT} unit.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class ExtendedCompositeUnitRegistryTest extends BaseUnitRegistryTest {

    public ExtendedCompositeUnitRegistryTest() {
        super(new CompositeUnitRegistry(new UnitsRegistry(), new CountUnitRegistry()));
    }

    @Override
    public void testConversionOf_count() throws Exception {
        try {
            super.testConversionOf_count();
        } catch (AssertionError e) {
            Assertions.assertThat(registry.lookup(DlmsUnit.COUNT)).contains(Units.ONE);
        }
    }

    static class CountUnitRegistry implements UnitRegistry {

        @Override
        public Optional<Unit<?>> lookup(DlmsUnit wmbusType) {
            if (DlmsUnit.COUNT.equals(wmbusType)) {
                return Optional.of(Units.ONE);
            }

            return Optional.empty();
        }

        @Override
        public Optional<Class<? extends Quantity<?>>> quantity(@Nullable DlmsUnit wmbusType) {
            return Optional.empty();
        }
    }
}
