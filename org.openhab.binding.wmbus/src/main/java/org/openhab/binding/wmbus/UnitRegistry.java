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
package org.openhab.binding.wmbus;

import java.util.Optional;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openmuc.jmbus.DlmsUnit;

/**
 * Definition of unit registry which allows to provide mapping from wmbus types to javax.measure units.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
@NonNullByDefault
public interface UnitRegistry {

    /**
     * Provides mapping from wmbus type to javax.measure type.
     *
     * @param wmbusType Data value type as per wmbus/IEC standard.
     * @return Optional containing unit representing same value kind based on javax.measure types.
     */
    Optional<Unit<?>> lookup(@Nullable DlmsUnit wmbusType);

    /**
     * Provides information of what kind of quantity given dlms unit is - ie. Power, Force.
     *
     * @param wmbusType Data value type as per wmbus/IEC standard.
     * @return Optional containing quantity type according to unit mapping.
     */
    Optional<Class<? extends Quantity<?>>> quantity(@Nullable DlmsUnit wmbusType);
}
