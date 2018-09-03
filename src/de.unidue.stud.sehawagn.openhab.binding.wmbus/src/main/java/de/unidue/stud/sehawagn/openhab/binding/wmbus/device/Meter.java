package de.unidue.stud.sehawagn.openhab.binding.wmbus.device;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
