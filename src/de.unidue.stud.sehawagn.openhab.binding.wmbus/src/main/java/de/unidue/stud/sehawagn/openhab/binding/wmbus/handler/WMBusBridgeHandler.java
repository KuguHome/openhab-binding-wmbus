package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusConnection;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusManufacturer;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusSerialBuilder;
import org.openmuc.jmbus.wireless.WMBusMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusDevice;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusReceiver;

/**
 * This class represents the WMBus bridge and handles general events for the whole group of WMBus devices.
 */
public class WMBusBridgeHandler extends ConfigStatusBridgeHandler {

	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(WMBusBindingConstants.THING_TYPE_BRIDGE);

	private static final String DEVICE_STATE_ADDED = "added";

	private static final String DEVICE_STATE_CHANGED = "changed";

	private Logger logger = LoggerFactory.getLogger(WMBusBridgeHandler.class);

	private WMBusReceiver wmbusReceiver = null;
	private WMBusConnection wmbusConnection = null;

	private Map<String, WMBusDevice> knownDevices = new HashMap<>();

	private List<WMBusMessageListener> wmBusMessageListeners = new CopyOnWriteArrayList<>();

	// TODO HashMap would be the unsynchronized version; differences see here: https://stackoverflow.com/questions/40471/differences-between-hashmap-and-hashtable#40878
	private Map<SecondaryAddress, byte[]> encryptionKeys = new Hashtable<SecondaryAddress, byte[]>();

	public WMBusBridgeHandler(Bridge bridge) {
		super(bridge);
	}

	@Override
	public Collection<ConfigStatusMessage> getConfigStatus() {
		return Collections.emptyList(); // all good, otherwise add some messages
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		// judging from the hue bridge, this seems to be not needed...?
		logger.debug("WARNING: Unexpected call of handleCommand(). Parameters are channelUID={} and command={}", channelUID, command);
	}

	/**
	 * Connects to the WMBus radio module and updates bridge status.
	 *
	 * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
	 */
	@Override
	public void initialize() {
		logger.debug("WMBusBridgeHandler: initialize()");

		// check stick model
		if (!getConfig().containsKey(WMBusBindingConstants.CONFKEY_STARTUP_DELAY) || getConfig().get(WMBusBindingConstants.CONFKEY_STARTUP_DELAY) == null) {
			logger.error("Cannot open WMBus device. Startup delay not given.");
			updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Cannot open WMBus device. Startup delay not given.");
			return;
		}
		int startupDelay = ((BigDecimal) getConfig().get(WMBusBindingConstants.CONFKEY_STARTUP_DELAY)).intValue();
		if (startupDelay > 0) {
			logger.debug("WMBusBridgeHandler: waiting {} seconds to let other bindings using serial interfaces start up.", startupDelay);

			try {
				Thread.sleep(1000 * startupDelay);
			} catch (InterruptedException e) {
				logger.debug("WMBusHandlerFactory: returned early out of thread sleep :-(");
			}
		}

		// check stick model
		if (!getConfig().containsKey(WMBusBindingConstants.CONFKEY_STICK_MODEL) || getConfig().get(WMBusBindingConstants.CONFKEY_STICK_MODEL) == null || ((String) getConfig().get(WMBusBindingConstants.CONFKEY_STICK_MODEL)).isEmpty()) {
			logger.error("Cannot open WMBus device. Stick model not given.");
			updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Cannot open WMBus device. Stick model not given.");
			return;
		}

		// check serial device name
		if (!getConfig().containsKey(WMBusBindingConstants.CONFKEY_INTERFACE_NAME) || getConfig().get(WMBusBindingConstants.CONFKEY_INTERFACE_NAME) == null || ((String) getConfig().get(WMBusBindingConstants.CONFKEY_RADIO_MODE)).isEmpty()) {
			logger.error("Cannot open WMBus device. Serial device name not given.");
			updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Cannot open WMBus device. Serial device name not given.");
			return;
		}

		// check radio mode
		if (!getConfig().containsKey(WMBusBindingConstants.CONFKEY_RADIO_MODE) || getConfig().get(WMBusBindingConstants.CONFKEY_RADIO_MODE) == null || ((String) getConfig().get(WMBusBindingConstants.CONFKEY_RADIO_MODE)).isEmpty()) {
			logger.error("Cannot open WMBus device. Radio mode not given.");
			updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Cannot open WMBus device. Radio mode not given.");
			return;
		}

		// set up WMBus receiver = handler for radio telegrams
		if (wmbusReceiver == null) {
			String stickModel = (String) getConfig().get(WMBusBindingConstants.CONFKEY_STICK_MODEL);
			String interfaceName = (String) getConfig().get(WMBusBindingConstants.CONFKEY_INTERFACE_NAME);
			String radioModeStr = (String) getConfig().get(WMBusBindingConstants.CONFKEY_RADIO_MODE);

			// connect to the radio module / open WMBus connection
			logger.debug("Opening wmbus stick {} serial port {} in mode {}", stickModel, interfaceName, radioModeStr);

			WMBusManufacturer wmBusManufacturer = parseManufacturer(stickModel);
			if (wmBusManufacturer == null) {
				logger.error("Cannot open WMBus device. Unknown manufacturer given: " + stickModel + ". Expected 'amber' or 'imst' or 'rc'.");
				updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Cannot open WMBus device. Unknown manufacturer given: " + stickModel + ". Expected 'amber' or 'imst' or 'rc'.");
				return;
			}
			logger.debug("Building new connection");

			wmbusReceiver = new WMBusReceiver(this);

			if (!getConfig().containsKey(WMBusBindingConstants.CONFKEY_DEVICEID_FILTER) || getConfig().get(WMBusBindingConstants.CONFKEY_DEVICEID_FILTER) == null || ((String) getConfig().get(WMBusBindingConstants.CONFKEY_DEVICEID_FILTER)).isEmpty()) {
				logger.debug("Device ID filter is empty.");
			} else {
				wmbusReceiver.setFilterIDs(parseDeviceIDFilter());
			}

			WMBusSerialBuilder connectionBuilder = new WMBusSerialBuilder(wmBusManufacturer, wmbusReceiver, interfaceName);

			WMBusMode radioMode;
			// check and convert radio mode
			switch (radioModeStr) {
			case "S":
				radioMode = WMBusMode.S;
				break;
			case "T":
				radioMode = WMBusMode.T;
				break;
			case "C":
				radioMode = WMBusMode.C;
				break;
			default:
				logger.error("Cannot open WMBus device. Unknown radio mode given: " + radioModeStr + ". Expected 'S', 'T', or 'C'.");
				updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Cannot open WMBus device. Unknown radio mode given: " + radioModeStr + ". Expected 'S', 'T', or 'C'.");
				return;
			}
			logger.debug("Setting WMBus radio mode to {}", radioMode.toString());
			connectionBuilder.setMode(radioMode);
			connectionBuilder.setTimeout(0); // infinite

			try {
				logger.debug("Building/opening connection");
				logger.debug("NOTE: if initialization does not progress from here, check systemd journal for Execptions -- probably native lib still loaded by another ClassLoader = previous version or instance of WMBus binding -> restart OpenHAB");
				if (wmbusConnection != null) {
					logger.debug("Connection already set, closing old");
					wmbusConnection.close();
					wmbusConnection = null;
				}
				wmbusConnection = connectionBuilder.build();
			} catch (IOException e) {
				logger.error("Cannot open WMBus device. Connection builder returned: " + e.getMessage());
				updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Cannot open WMBus device. Connection builder returned: " + e.getMessage());
				return;
			}

			logger.debug("Connected to WMBus serial port");

			// close WMBus connection on shutdown
			logger.trace("Setting shutdown hook");
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					if (wmbusConnection != null) {
						try {
							logger.debug("Closing connection to WMBus radio device");
							wmbusConnection.close();
						} catch (IOException e) {
							logger.error("Cannot close connection to WMBus radio module: " + e.getMessage());
							updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Cannot close connection to WMBus radio module: " + e.getMessage());
							return;
						}
					}
				}
			});

			if (!getConfig().containsKey(WMBusBindingConstants.CONFKEY_ENCRYPTION_KEYS) || getConfig().get(WMBusBindingConstants.CONFKEY_ENCRYPTION_KEYS) == null || ((String) getConfig().get(WMBusBindingConstants.CONFKEY_ENCRYPTION_KEYS)).isEmpty()) {
				logger.debug("No encryption keys given.");
			} else {
				logger.trace("Parsing given encryption keys");
				parseKeys();
				logger.trace("Encryption keys parsed");
				Map<SecondaryAddress, byte[]> encryptionKeys = getEncryptionKeys();
				logger.trace("Setting encryption keys in JMBus, count: " + encryptionKeys.size());
				for (Entry<SecondaryAddress, byte[]> encryptionKey : encryptionKeys.entrySet()) {
					wmbusConnection.addKey(encryptionKey.getKey(), encryptionKey.getValue());
				}
				logger.trace("Keys successfully set");
			}

			// success
			logger.debug("WMBusBridgeHandler: Initialization done! Setting bridge online");
			updateStatus(ThingStatus.ONLINE);
		}
	}

	private Map<SecondaryAddress, byte[]> getEncryptionKeys() {
		return encryptionKeys;
	}

	private static WMBusManufacturer parseManufacturer(String manufacturer) {
		switch (manufacturer.toLowerCase()) {
		case "amber":
			return WMBusManufacturer.AMBER;
		case "rc":
			return WMBusManufacturer.RADIO_CRAFTS;
		case "imst":
			return WMBusManufacturer.IMST;
		default:
			return null;
		}
	}

	public boolean registerWMBusMessageListener(WMBusMessageListener wmBusMessageListener) {
		if (wmBusMessageListener == null) {
			throw new NullPointerException("It's not allowed to pass a null WMBusMessageListener.");
		}
		logger.debug("register listener: Adding");
		boolean result = wmBusMessageListeners.add(wmBusMessageListener);
		logger.debug("register listener: Success");
		if (result) {
			// inform the listener initially about all devices and their states
			for (WMBusDevice device : knownDevices.values()) {
				wmBusMessageListener.onNewWMBusDevice(device);
			}
		}
		return result;
	}

	/**
	 * Iterate through wmBusMessageListeners and notify them about a newly received message.
	 *
	 * @param device
	 */
	private void notifyWMBusMessageListeners(final WMBusDevice device, final String type) {
		logger.trace("bridge: notify message listeners: sending to all");
		for (WMBusMessageListener wmBusMessageListener : wmBusMessageListeners) {
			try {
				switch (type) {
				case DEVICE_STATE_ADDED: {
					wmBusMessageListener.onNewWMBusDevice(device);
					break;
				}
				case DEVICE_STATE_CHANGED: {
					wmBusMessageListener.onChangedWMBusDevice(device);
					break;
				}
				default: {
					throw new IllegalArgumentException("Could not notify wmBusMessageListeners for unknown event type " + type);
				}
				}
			} catch (Exception e) {
				logger.error("An exception occurred while notifying the WMBusMessageListener", e);
			}
		}
		logger.trace("bridge: notify message listeners: return");
	}

	@Override
	public void dispose() {
		logger.debug("WMBus bridge Handler disposed.");

		if (wmbusConnection != null) {
			logger.debug("Close serial device connection");
			try {
				wmbusConnection.close();
			} catch (IOException e) {
				logger.error("An exception occurred while closing the wmbusConnection", e);
			}
			wmbusConnection = null;
		}

		if (wmbusReceiver != null) {
			wmbusReceiver = null;
		}
	}

	public void processMessage(WMBusDevice device) {
		logger.trace("bridge: processMessage begin");
		String deviceId = device.getDeviceId();
		String deviceState = DEVICE_STATE_ADDED;
		if (knownDevices.containsKey(deviceId)) {
			deviceState = DEVICE_STATE_CHANGED;
		}
		knownDevices.put(deviceId, device);
		logger.trace("bridge processMessage: notifying listeners");
		notifyWMBusMessageListeners(device, deviceState);
		logger.trace("bridge: processMessage end");
	}

	public WMBusDevice getDeviceById(String deviceId) {
		logger.trace("bridge: get device by id: " + deviceId);
		if (knownDevices.containsKey(deviceId)) {
			logger.trace("bridge: found device");
		} else {
			logger.trace("bridge: device not found");
		}
		return knownDevices.get(deviceId);
	}

	private void parseKeys() {
		String[] idKeyPairs = ((String) getConfig().get(WMBusBindingConstants.CONFKEY_ENCRYPTION_KEYS)).split(";");
		for (String currentKey : idKeyPairs) {
			String[] idKeyPair = currentKey.split(":");
			if (idKeyPair.length != 2) {
				logger.error("parseKeys(): A key has to be a given as [secondary address]:[key].", true);
			} else {
				int secondaryAddressLength = idKeyPair[0].length();
				if (secondaryAddressLength != 16) {
					logger.error("parseKeys(): The secondary address needs to be 16 digits long, but has " + secondaryAddressLength + '.', true);
				} else {
					try {
						byte[] secondaryAddressbytes = DatatypeConverter.parseHexBinary(idKeyPair[0]);
						SecondaryAddress secondaryAddress = SecondaryAddress.newFromWMBusLlHeader(secondaryAddressbytes, 0);
						try {
							byte[] key = DatatypeConverter.parseHexBinary(idKeyPair[1]);
							encryptionKeys.put(secondaryAddress, key);
						} catch (IllegalArgumentException e) {
							logger.error("parseKeys(): The key is not hexadecimal.", true);
						}
					} catch (NumberFormatException e) {
						logger.error("parseKeys(): The secondary address is not hexadecimal.", true);
					}
				}
			}
		}
	}

	private int[] parseDeviceIDFilter() {
		String[] ids = ((String) getConfig().get(WMBusBindingConstants.CONFKEY_DEVICEID_FILTER)).split(";");
		int[] idInts = new int[ids.length];
		for (int i = 0; i < ids.length; i++) {
			String curID = ids[i];
			idInts[i] = Integer.valueOf(curID);
		}
		return idInts;
	}

}
