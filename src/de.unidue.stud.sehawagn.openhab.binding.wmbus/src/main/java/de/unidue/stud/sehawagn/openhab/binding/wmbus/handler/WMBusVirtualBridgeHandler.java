package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static java.util.Collections.emptyMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.VirtualWMBusMessageHelper;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusReceiver;

/**
 * This class represents the WMBus bridge and handles general events for the whole group of WMBus devices.
 */
public class WMBusVirtualBridgeHandler extends WMBusBridgeHandler {

	public static final String CHANNEL_CODE_VIRTUAL_BRIDGE = "wmbusvirtualbridge_code";

	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
			.singleton(WMBusBindingConstants.THING_TYPE_VIRTUAL_BRIDGE);

	private Logger logger = LoggerFactory.getLogger(WMBusVirtualBridgeHandler.class);

	public WMBusVirtualBridgeHandler(Bridge bridge) {
		super(bridge);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.trace("handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: "
				+ command.toString());

		if (command == RefreshType.REFRESH) {
			logger.trace("handleCommand(): (2/5) command.refreshtype == REFRESH");

			byte[] controlField = DatatypeConverter
					.parseHexBinary((String) getConfig().get(WMBusBindingConstants.CONFKEY_VIRTUAL_CF));
			byte[] bytes = DatatypeConverter
					.parseHexBinary((String) getConfig().get(WMBusBindingConstants.CONFKEY_VIRTUAL_BYTES));
			byte[] address = DatatypeConverter
					.parseHexBinary((String) getConfig().get(WMBusBindingConstants.CONFKEY_VIRTUAL_ADDRESS));

			Byte rssi = Byte.valueOf((String) getConfig().get(WMBusBindingConstants.CONFKEY_VIRTUAL_RSSI));

			byte[] message = ArrayUtils.addAll(controlField, ArrayUtils.addAll(address, ArrayUtils.add(bytes, rssi)));

			byte[] size = new byte[] { (byte) message.length };

			message = ArrayUtils.addAll(size, message);

			SecondaryAddress secondaryAddress = SecondaryAddress.newFromWMBusLlHeader(address, 2);

			Map<SecondaryAddress, byte[]> keyMap = new HashMap<SecondaryAddress, byte[]>() {
				{
					put(secondaryAddress, new byte[0]);
				}
			};

			Map<SecondaryAddress, byte[]> ekeyMap = emptyMap();
			WMBusMessage wmBusMessage = null;
			try {
				wmBusMessage = VirtualWMBusMessageHelper.decode(message, 100, ekeyMap);
			} catch (DecodingException e) {
				e.printStackTrace();
			}

			wmbusReceiver.newMessage(wmBusMessage);

		}
	}

	/**
	 * Connects to the WMBus radio module and updates bridge status.
	 *
	 * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
	 */
	@Override
	public void initialize() {
		logger.debug("WMBusVirtualBridgeHandler: initialize()");
		if (wmbusReceiver == null) {
			wmbusReceiver = new WMBusReceiver(this);
		}
		// success
		logger.debug("WMBusVirtualBridgeHandler: Initialization done! Setting bridge online");
		updateStatus(ThingStatus.ONLINE);

		updateState(getThing().getChannel(CHANNEL_CODE_VIRTUAL_BRIDGE).getUID(), new StringType("FFFFFF"));

	}

	@Override
	public void handleConfigurationUpdate(@NonNull Map<@NonNull String, @NonNull Object> configurationParameters) {
		super.handleConfigurationUpdate(configurationParameters);
		postCommand(getThing().getChannel(CHANNEL_CODE_VIRTUAL_BRIDGE).getUID(), RefreshType.REFRESH);
	}

}
