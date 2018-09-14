package de.unidue.stud.sehawagn.openhab.binding.wmbus.device;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusDeviceHandler;

@Component(service = { UnknownDevice.class }, properties = "OSGI-INF/unknown.properties")
public class UnknownDevice extends Meter {

	public static final Logger logger = LoggerFactory.getLogger(UnknownDevice.class);

	@Activate
	protected void activate(Map<String, String> properties) {
	}

	@Deactivate
	protected void deactivate() {
	}

	public class UnknownWMBusDeviceHandler extends WMBusDeviceHandler {

		public UnknownWMBusDeviceHandler(Thing thing) {
			super(thing);
		}

		@Override
		public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
			logger.trace("handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: "
					+ command.toString());
		}

	}
}
