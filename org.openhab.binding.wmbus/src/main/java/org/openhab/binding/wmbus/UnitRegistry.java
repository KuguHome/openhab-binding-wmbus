/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus;

import java.util.Optional;

import javax.measure.Unit;

import org.openmuc.jmbus.DlmsUnit;

/**
 * Definition of unit registry which allows to provide mapping from wmbus types to javax.measure units.
 *
 * @author Łukasz Dywicki - Initial contribution.
 *
 */
public interface UnitRegistry {

    /**
     * Provides mapping from wmbus type to javax.measure type.
     *
     * @param wmbusType Data value type as per wmbus/IEC standard.
     * @return Optional containing unit representing same value kind based on javax.measure types.
     */
    Optional<Unit<?>> lookup(DlmsUnit wmbusType);

}
