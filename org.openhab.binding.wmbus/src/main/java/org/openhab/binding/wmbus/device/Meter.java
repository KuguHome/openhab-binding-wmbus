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

package org.openhab.binding.wmbus.device;

import java.util.Set;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Meter} class defines common Meter device
 *
 * @author Roman Malyugin - Initial contribution
 */

public class Meter {

    protected Set<ThingTypeUID> supportedThingTypes;
    protected ThingTypeUID thingType;
    protected String thingTypeName;
    protected String thingTypeId;

    public Set<ThingTypeUID> getSupportedThingTypes() {
        return supportedThingTypes;
    }

    public ThingTypeUID getThingType() {
        return thingType;
    }

    public String getThingTypeName() {
        return thingTypeName;
    }

    public String getThingTypeId() {
        return thingTypeId;
    }
}
