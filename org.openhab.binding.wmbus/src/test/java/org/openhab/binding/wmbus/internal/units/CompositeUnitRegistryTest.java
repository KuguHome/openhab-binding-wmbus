/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.internal.units;

/**
 * Test of {@link CompositeUnitRegistry} with {@link SmartHomeUnitsRegistry}.
 *
 * They should behave in same way together.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class CompositeUnitRegistryTest extends BaseUnitRegistryTest {

    public CompositeUnitRegistryTest() {
        super(new CompositeUnitRegistry(new SmartHomeUnitsRegistry()));
    }

}
