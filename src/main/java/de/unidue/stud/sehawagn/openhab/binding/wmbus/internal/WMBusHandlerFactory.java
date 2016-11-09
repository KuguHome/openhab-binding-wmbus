package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusBridgeHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusTechemHKVHandler;

public class WMBusHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(WMBusBindingConstants.THING_TYPE_WMBUS_BRIDGE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {

        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_WMBUS_BRIDGE)) {
            if (thing instanceof Bridge) {
                return new WMBusBridgeHandler((Bridge) thing);
            } else {
                return null;
            }
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_WMBUS_TECHEM_HKV)) {
            return new WMBusTechemHKVHandler(thing);
        } else {
            return null;
        }
    }

}
