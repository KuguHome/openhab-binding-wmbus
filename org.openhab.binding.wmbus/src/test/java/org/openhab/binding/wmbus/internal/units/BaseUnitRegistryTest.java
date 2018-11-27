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

import javax.measure.Unit;

import org.assertj.core.api.Assertions;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.junit.Test;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openmuc.jmbus.DlmsUnit;

/**
 * Test of unit conversion assuming just framework measure units registry.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public abstract class BaseUnitRegistryTest {

    protected final UnitRegistry registry;

    protected BaseUnitRegistryTest(UnitRegistry registry) {
        this.registry = registry;
    }

    @Test
    public void testConversionOf_ampere() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.AMPERE)).hasValue(SmartHomeUnits.AMPERE);
    }

    @Test
    public void testConversionOf_ampere_hour() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.AMPERE_HOUR)).isEmpty();
    }

    @Test
    public void testConversionOf_ampere_per_metre() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.AMPERE_PER_METRE)).isEmpty();
    }

    @Test
    public void testConversionOf_ampere_squared_hours() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.AMPERE_SQUARED_HOURS)).isEmpty();
    }

    @Test
    public void testConversionOf_ampere_squared_hour_meter_constant_or_pulse_value() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.AMPERE_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE)).isEmpty();
    }

    @Test
    public void testConversionOf_apparent_energy_meter_constant_or_pulse_value() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.APPARENT_ENERGY_METER_CONSTANT_OR_PULSE_VALUE)).isEmpty();
    }

    @Test
    public void testConversionOf_bar() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.BAR)).isEmpty();
    }

    @Test
    public void testConversionOf_calorific_value() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.CALORIFIC_VALUE)).isEmpty();
    }

    @Test
    public void testConversionOf_coulomb() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.COULOMB)).contains(SmartHomeUnits.COULOMB);
    }

    @Test
    public void testConversionOf_count() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.COUNT)).isEmpty();
    }

    @Test
    public void testConversionOf_cubic_feet() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.CUBIC_FEET)).contains(ImperialUnits.CUBIC_FOOT);
    }

    @Test
    public void testConversionOf_cubic_metre_corrected() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.CUBIC_METRE)).contains(SIUnits.CUBIC_METRE);
        Assertions.assertThat(lookup(DlmsUnit.CUBIC_METRE_CORRECTED)).contains(SIUnits.CUBIC_METRE);
    }

    @Test
    public void testConversionOf_cubic_metre_per_day_corrected() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.CUBIC_METRE_PER_DAY)).isEmpty();
        Assertions.assertThat(lookup(DlmsUnit.CUBIC_METRE_PER_DAY_CORRECTED)).isEmpty();
    }

    @Test
    public void testConversionOf_cubic_metre_per_hour_corrected() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.CUBIC_METRE_PER_HOUR)).isEmpty();
        Assertions.assertThat(lookup(DlmsUnit.CUBIC_METRE_PER_HOUR_CORRECTED)).isEmpty();
    }

    @Test
    public void testConversionOf_cubic_metre_per_minute() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.CUBIC_METRE_PER_MINUTE)).isEmpty();
    }

    @Test
    public void testConversionOf_cubic_metre_per_second() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.CUBIC_METRE_PER_SECOND)).isEmpty();
    }

    @Test
    public void testConversionOf_currency() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.CURRENCY)).isEmpty();
    }

    @Test
    public void testConversionOf_day() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.DAY)).contains(SmartHomeUnits.DAY);
    }

    @Test
    public void testConversionOf_degree() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.DEGREE)).isEmpty();
    }

    @Test
    public void testConversionOf_degree_celsius() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.DEGREE_CELSIUS)).contains(SIUnits.CELSIUS);
    }

    @Test
    public void testConversionOf_degree_fahrenheit() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.DEGREE_FAHRENHEIT)).isEmpty();
    }

    @Test
    public void testConversionOf_energy_per_volume() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.ENERGY_PER_VOLUME)).isEmpty();
    }

    @Test
    public void testConversionOf_farad() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.FARAD)).contains(SmartHomeUnits.FARAD);
    }

    @Test
    public void testConversionOf_henry() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.HENRY)).contains(SmartHomeUnits.HENRY);
    }

    @Test
    public void testConversionOf_hertz() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.HERTZ)).contains(SmartHomeUnits.HERTZ);
    }

    @Test
    public void testConversionOf_hour() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.HOUR)).contains(SmartHomeUnits.HOUR);
    }

    @Test
    public void testConversionOf_joule() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.JOULE)).contains(SmartHomeUnits.JOULE);
    }

    @Test
    public void testConversionOf_joule_per_hour() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.JOULE_PER_HOUR)).isEmpty();
    }

    @Test
    public void testConversionOf_kelvin() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.KELVIN)).contains(SmartHomeUnits.KELVIN);
    }

    @Test
    public void testConversionOf_kilogram() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.KILOGRAM)).contains(SIUnits.KILOGRAM);
    }

    @Test
    public void testConversionOf_kilogram_per_hour() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.KILOGRAM_PER_HOUR)).isEmpty();
    }

    @Test
    public void testConversionOf_kilogram_per_second() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.KILOGRAM_PER_SECOND)).isEmpty();
    }

    @Test
    public void testConversionOf_litre() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.LITRE)).contains(SmartHomeUnits.LITRE);
    }

    @Test
    public void testConversionOf_mass_density() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.MASS_DENSITY)).isEmpty();
    }

    @Test
    public void testConversionOf_meter_constant_or_pulse_value() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.METER_CONSTANT_OR_PULSE_VALUE)).isEmpty();
    }

    @Test
    public void testConversionOf_metre() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.METRE)).contains(SIUnits.METRE);
    }

    @Test
    public void testConversionOf_metre_per_second() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.METRE_PER_SECOND)).contains(SmartHomeUnits.METRE_PER_SECOND);
    }

    @Test
    public void testConversionOf_min() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.MIN)).contains(SmartHomeUnits.MINUTE);
    }

    @Test
    public void testConversionOf_mole_percent() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.MOLE_PERCENT)).isEmpty();
    }

    @Test
    public void testConversionOf_month() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.MONTH)).contains(SmartHomeUnits.FARAD);
    }

    @Test
    public void testConversionOf_newton() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.NEWTON)).contains(SmartHomeUnits.NEWTON);
    }

    @Test
    public void testConversionOf_newtonmeter() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.NEWTONMETER)).isEmpty();
    }

    @Test
    public void testConversionOf_ohm() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.OHM)).contains(SmartHomeUnits.OHM);
    }

    @Test
    public void testConversionOf_ohm_metre() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.OHM_METRE)).isEmpty();
    }

    @Test
    public void testConversionOf_other_unit() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.OTHER_UNIT)).isEmpty();
    }

    @Test
    public void testConversionOf_pascal() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.PASCAL)).contains(SIUnits.PASCAL);
    }

    @Test
    public void testConversionOf_pascal_second() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.PASCAL_SECOND)).isEmpty();
    }

    @Test
    public void testConversionOf_percentage() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.PERCENTAGE)).isEmpty();
    }

    @Test
    public void testConversionOf_reactive_energy_meter_constant_or_pulse_value() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.REACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE)).isEmpty();
    }

    @Test
    public void testConversionOf_reserved() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.RESERVED)).isEmpty();
    }

    @Test
    public void testConversionOf_second() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.SECOND)).contains(SmartHomeUnits.SECOND);
    }

    @Test
    public void testConversionOf_signal_strength() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.SIGNAL_STRENGTH)).isEmpty();
    }

    @Test
    public void testConversionOf_specific_energy() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.SPECIFIC_ENERGY)).isEmpty();
    }

    @Test
    public void testConversionOf_tesla() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.TESLA)).contains(SmartHomeUnits.TESLA);
    }

    @Test
    public void testConversionOf_us_gallon() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.US_GALLON)).isEmpty();
    }

    @Test
    public void testConversionOf_us_gallon_per_hour() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.US_GALLON_PER_HOUR)).isEmpty();
    }

    @Test
    public void testConversionOf_us_gallon_per_minute() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.US_GALLON_PER_MINUTE)).isEmpty();
    }

    @Test
    public void testConversionOf_var() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.VAR)).isEmpty();
    }

    @Test
    public void testConversionOf_var_hour() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.VAR_HOUR)).isEmpty();
    }

    @Test
    public void testConversionOf_volt() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.VOLT)).contains(SmartHomeUnits.VOLT);
    }

    @Test
    public void testConversionOf_volt_ampere() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.VOLT_AMPERE)).isEmpty();
    }

    @Test
    public void testConversionOf_volt_ampere_hour() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.VOLT_AMPERE_HOUR)).isEmpty();
    }

    @Test
    public void testConversionOf_volt_per_metre() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.VOLT_PER_METRE)).isEmpty();
    }

    @Test
    public void testConversionOf_volt_squared_hours() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.VOLT_SQUARED_HOURS)).isEmpty();
    }

    @Test
    public void testConversionOf_volt_squared_hour_meter_constant_or_pulse_value() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.VOLT_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE)).isEmpty();
    }

    @Test
    public void testConversionOf_watt() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.WATT)).contains(SmartHomeUnits.WATT);
    }

    @Test
    public void testConversionOf_watt_hour() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.WATT_HOUR)).contains(SmartHomeUnits.WATT_HOUR);
    }

    @Test
    public void testConversionOf_weber() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.WEBER)).contains(SmartHomeUnits.WEBER);
    }

    @Test
    public void testConversionOf_week() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.WEEK)).contains(SmartHomeUnits.WEEK);
    }

    @Test
    public void testConversionOf_year() throws Exception {
        Assertions.assertThat(lookup(DlmsUnit.YEAR)).contains(SmartHomeUnits.YEAR);
    }

    protected Optional<Unit<?>> lookup(DlmsUnit wmbusType) {
        return registry.lookup(wmbusType);
    }

}
