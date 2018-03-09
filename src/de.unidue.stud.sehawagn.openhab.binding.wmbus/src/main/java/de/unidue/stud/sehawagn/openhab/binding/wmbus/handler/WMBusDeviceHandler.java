package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WMBusDeviceHandler extends BaseThingHandler implements WMBusMessageListener {
	private final Logger logger = LoggerFactory.getLogger(WMBusDeviceHandler.class);

	public WMBusDeviceHandler(Thing thing) {
		super(thing);
		logger.debug("new() for Thing" + thing.toString());
	}
}
