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
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCapacitance;
import javax.measure.quantity.ElectricCharge;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricInductance;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.ElectricResistance;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Force;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.MagneticFlux;
import javax.measure.quantity.MagneticFluxDensity;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;
import javax.measure.quantity.Volume;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openmuc.jmbus.DlmsUnit;

/**
 * Lookup table between wmbus and smart home units which contains default mapping of units.
 *
 * Since there are many units which are not covered by Eclipse SmartHome there are empty case
 * statements. These are left for future to gets filled in once support for them is present.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class SmartHomeUnitsRegistry implements UnitRegistry {

    @Override
    public Optional<Unit<?>> lookup(DlmsUnit wmbusType) {
        if (wmbusType == null) {
            return Optional.empty();
        }

        switch (wmbusType) {
            case AMPERE:
                return Optional.of(SmartHomeUnits.AMPERE);
            case AMPERE_HOUR:
                break;
            case AMPERE_PER_METRE:
                break;
            case AMPERE_SQUARED_HOURS:
                break;
            case AMPERE_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case APPARENT_ENERGY_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case BAR:
                // Not present in ESH 0.9 / 0.10.0.oh230
                // return Optional.of(SmartHomeUnits.BAR);
                break;
            case CALORIFIC_VALUE:
                break;
            case COULOMB:
                return Optional.of(SmartHomeUnits.COULOMB);
            case COUNT:
                break;
            case CUBIC_FEET:
                return Optional.of(ImperialUnits.CUBIC_FOOT);
            case CUBIC_METRE:
            case CUBIC_METRE_CORRECTED:
                return Optional.of(SIUnits.CUBIC_METRE);
            case CUBIC_METRE_PER_DAY:
            case CUBIC_METRE_PER_DAY_CORRECTED:
            case CUBIC_METRE_PER_HOUR:
            case CUBIC_METRE_PER_HOUR_CORRECTED:
            case CUBIC_METRE_PER_MINUTE:
            case CUBIC_METRE_PER_SECOND:
                // there is no support for VolumetricFlowRate yet.
                return Optional.empty();
            case CURRENCY:
                break;
            case DAY:
                return Optional.of(SmartHomeUnits.DAY);
            case DEGREE:
                break;
            case DEGREE_CELSIUS:
                return Optional.of(SIUnits.CELSIUS);
            case DEGREE_FAHRENHEIT:
                break;
            case ENERGY_PER_VOLUME:
                break;
            case FARAD:
                return Optional.of(SmartHomeUnits.FARAD);
            case HENRY:
                return Optional.of(SmartHomeUnits.HENRY);
            case HERTZ:
                return Optional.of(SmartHomeUnits.HERTZ);
            case HOUR:
                return Optional.of(SmartHomeUnits.HOUR);
            case JOULE:
                return Optional.of(SmartHomeUnits.JOULE);
            case JOULE_PER_HOUR:
                break;
            case KELVIN:
                return Optional.of(SmartHomeUnits.KELVIN);
            case KILOGRAM:
                return Optional.of(SIUnits.KILOGRAM);
            case KILOGRAM_PER_HOUR:
                break;
            case KILOGRAM_PER_SECOND:
                break;
            case LITRE:
                return Optional.of(SmartHomeUnits.LITRE);
            case MASS_DENSITY:
                break;
            case METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case METRE:
                return Optional.of(SIUnits.METRE);
            case METRE_PER_SECOND:
                return Optional.of(SmartHomeUnits.METRE_PER_SECOND);
            case MIN:
                return Optional.of(SmartHomeUnits.MINUTE);
            case MOLE_PERCENT:
                break;
            case MONTH:
                return Optional.of(SmartHomeUnits.FARAD);
            case NEWTON:
                return Optional.of(SmartHomeUnits.NEWTON);
            case NEWTONMETER:
                break;
            case OHM:
                return Optional.of(SmartHomeUnits.OHM);
            case OHM_METRE:
                break;
            case OTHER_UNIT:
                break;
            case PASCAL:
                return Optional.of(SIUnits.PASCAL);
            case PASCAL_SECOND:
                break;
            case PERCENTAGE:
                break;
            case REACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case RESERVED:
                break;
            case SECOND:
                return Optional.of(SmartHomeUnits.SECOND);
            case SIGNAL_STRENGTH:
                break;
            case SPECIFIC_ENERGY:
                break;
            case TESLA:
                return Optional.of(SmartHomeUnits.TESLA);
            case US_GALLON:
                break;
            case US_GALLON_PER_HOUR:
                break;
            case US_GALLON_PER_MINUTE:
                break;
            case VAR:
                break;
            case VAR_HOUR:
                break;
            case VOLT:
                return Optional.of(SmartHomeUnits.VOLT);
            case VOLT_AMPERE:
                break;
            case VOLT_AMPERE_HOUR:
                break;
            case VOLT_PER_METRE:
                break;
            case VOLT_SQUARED_HOURS:
                break;
            case VOLT_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case WATT:
                return Optional.of(SmartHomeUnits.WATT);
            case WATT_HOUR:
                return Optional.of(SmartHomeUnits.WATT_HOUR);
            case WEBER:
                return Optional.of(SmartHomeUnits.WEBER);
            case WEEK:
                return Optional.of(SmartHomeUnits.WEEK);
            case YEAR:
                return Optional.of(SmartHomeUnits.YEAR);
            default:
                break;
        }

        return Optional.empty();
    }

    @Override
    public Optional<Class<? extends Quantity<?>>> quantity(@Nullable DlmsUnit wmbusType) {

        if (wmbusType == null) {
            return Optional.empty();
        }

        switch (wmbusType) {
            case AMPERE:
                return Optional.of(ElectricCurrent.class);
            case AMPERE_HOUR:
                break;
            case AMPERE_PER_METRE:
                break;
            case AMPERE_SQUARED_HOURS:
                break;
            case AMPERE_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case APPARENT_ENERGY_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case BAR:
                return Optional.of(Pressure.class);
            case CALORIFIC_VALUE:
                break;
            case COULOMB:
                return Optional.of(ElectricCharge.class);
            case COUNT:
                break;
            case CUBIC_FEET:
            case CUBIC_METRE:
            case CUBIC_METRE_CORRECTED:
                return Optional.of(Volume.class);
            case CUBIC_METRE_PER_DAY:
            case CUBIC_METRE_PER_DAY_CORRECTED:
            case CUBIC_METRE_PER_HOUR:
            case CUBIC_METRE_PER_HOUR_CORRECTED:
            case CUBIC_METRE_PER_MINUTE:
            case CUBIC_METRE_PER_SECOND:
                // VolumetricFlow
                return Optional.empty();
            case CURRENCY:
                break;
            case DEGREE:
                return Optional.of(Angle.class);
            case DEGREE_CELSIUS:
            case DEGREE_FAHRENHEIT:
            case KELVIN:
                return Optional.of(Temperature.class);
            case ENERGY_PER_VOLUME:
                break;
            case FARAD:
                return Optional.of(ElectricCapacitance.class);
            case HENRY:
                return Optional.of(ElectricInductance.class);
            case HERTZ:
                return Optional.of(Frequency.class);
            case JOULE:
                return Optional.of(Energy.class);
            case JOULE_PER_HOUR:
                break;
            case KILOGRAM:
                return Optional.of(Mass.class);
            case KILOGRAM_PER_HOUR:
                return Optional.of(Speed.class);
            case KILOGRAM_PER_SECOND:
                break;
            case LITRE:
                return Optional.of(Volume.class);
            case MASS_DENSITY:
                return Optional.empty();
            case METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case METRE:
                return Optional.of(Length.class);
            case METRE_PER_SECOND:
                return Optional.of(Speed.class);
            case MOLE_PERCENT:
                break;
            case NEWTON:
                return Optional.of(Force.class);
            case NEWTONMETER:
                break;
            case OHM:
                return Optional.of(ElectricResistance.class);
            case OHM_METRE:
                break;
            case OTHER_UNIT:
                break;
            case PASCAL:
                return Optional.of(Pressure.class);
            case PASCAL_SECOND:
                break;
            case PERCENTAGE:
                return Optional.of(Dimensionless.class);
            case REACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case RESERVED:
                break;
            case SIGNAL_STRENGTH:
                break;
            case SPECIFIC_ENERGY:
                break;
            case TESLA:
                return Optional.of(MagneticFluxDensity.class);
            case US_GALLON:
                break;
            case US_GALLON_PER_HOUR:
            case US_GALLON_PER_MINUTE:
                // VolumetricFlow
                break;
            case VAR:
                break;
            case VAR_HOUR:
                break;
            case VOLT:
                return Optional.of(ElectricPotential.class);
            case VOLT_AMPERE:
                break;
            case VOLT_AMPERE_HOUR:
                break;
            case VOLT_PER_METRE:
                break;
            case VOLT_SQUARED_HOURS:
                break;
            case VOLT_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case WATT:
                return Optional.of(Power.class);
            case WATT_HOUR:
                return Optional.of(Energy.class);
            case WEBER:
                return Optional.of(MagneticFlux.class);
            case SECOND:
            case MIN:
            case HOUR:
            case DAY:
            case WEEK:
            case MONTH:
            case YEAR:
                return Optional.of(Time.class);
            default:
                break;
        }

        return Optional.empty();
    }

}
