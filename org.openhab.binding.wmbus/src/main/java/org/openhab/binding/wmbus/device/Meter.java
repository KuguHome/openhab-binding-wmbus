/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
