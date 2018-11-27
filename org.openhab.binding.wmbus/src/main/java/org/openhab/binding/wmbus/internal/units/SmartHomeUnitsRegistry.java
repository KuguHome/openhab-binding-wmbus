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
                break;
            case CUBIC_METRE_PER_HOUR:
            case CUBIC_METRE_PER_HOUR_CORRECTED:
                break;
            case CUBIC_METRE_PER_MINUTE:
                break;
            case CUBIC_METRE_PER_SECOND:
                break;
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

}
