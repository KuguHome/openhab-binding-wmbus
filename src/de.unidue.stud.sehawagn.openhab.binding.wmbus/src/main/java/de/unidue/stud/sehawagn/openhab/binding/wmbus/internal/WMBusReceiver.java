package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal;

import java.io.IOException;
import java.util.Arrays;

import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.VariableDataStructure;
import org.openmuc.jmbus.wireless.WMBusListener;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusBridgeHandler;

/**
 * Keeps connection with the WMBus radio module and forwards TODO Javadoc class description
 */
public class WMBusReceiver implements WMBusListener {

	int[] filterIDs = new int[] {};

	private WMBusBridgeHandler wmBusBridgeHandler;

	public static String VENDOR_TECHEM = "TCH";
	public static String VENDOR_QUNDIS = "QDS";
	public static String VENDOR_KAMSTRUP = "KAM";

	private final Logger logger = LoggerFactory.getLogger(WMBusReceiver.class);

	public WMBusReceiver(WMBusBridgeHandler wmBusBridgeHandler) {
		this.wmBusBridgeHandler = wmBusBridgeHandler;
	}

	public int[] getFilterIDs() {
		logger.debug("getFilterIDs(): got {}", Arrays.toString(filterIDs));
		return filterIDs;
	}

	public void setFilterIDs(int[] filterIDs) {
		logger.debug("setFilterIDs() to {}", Arrays.toString(filterIDs));
		this.filterIDs = filterIDs;
	}

	boolean filterMatch(int inQuestion) {
		logger.trace("filterMatch(): are we interested in device {}?", Integer.toString(inQuestion));
		if (filterIDs.length == 0) {
			logger.trace("filterMatch(): length is zero -> yes");
			return true;
		}
		for (int i = 0; i < filterIDs.length; i++) {
			if (filterIDs[i] == inQuestion) {
				logger.debug("filterMatch(): found the device -> yes");
				return true;
			}
		}
		logger.trace("filterMatch(): not found");
		return false;
	}

	/*
	 * Handle incoming WMBus message from radio module.
	 *
	 * @see org.openmuc.jmbus.WMBusListener#newMessage(org.openmuc.jmbus.WMBusMessage)
	 */
	@Override
	public void newMessage(WMBusMessage message) {
		logger.trace("newMessage(): new message received");
		WMBusDevice device = null;
		if (filterMatch(message.getSecondaryAddress().getDeviceId().intValue())) {
			logger.trace("newMessage(): Matched message received: {}", message.toString());
			// print basic info
			logger.debug("newMessage(): control field: {}, secondary address: {}", message.getControlField(), message.getSecondaryAddress().toString());
			// decode VDR
			VariableDataStructure vdr = message.getVariableDataResponse();
			try {
				vdr.decode();
				logger.trace("newMessage(): variable data response decoded");
				// VDR needs to be decoded for correct header information
				logger.trace("newMessage(): access number: {}, status: {}, encryption mode: {}, number of encrypted blocks: {}", vdr.getAccessNumber(), vdr.getStatus(), vdr.getEncryptionMode(), vdr.getNumberOfEncryptedBlocks());
				for (DataRecord record : vdr.getDataRecords()) {
					logger.trace("> record: {}", record.toString());
				}
				device = new WMBusDevice(message);
			} catch (DecodingException e) {
				logger.debug("newMessage(): access number: {}, status: {}, encryption mode: {}, number of encrypted blocks: {}", vdr.getAccessNumber(), vdr.getStatus(), vdr.getEncryptionMode(), vdr.getNumberOfEncryptedBlocks());
				logger.debug("newMessage(): could not decode as standard WMBus application layer message: {}", e.getMessage());
				device = new TechemHKV(message);
				try {
					logger.trace("newMessage(): try to decode as Techem message");
					device.decode();
					wmBusBridgeHandler.processMessage(device);
				} catch (DecodingException e1) {
					logger.debug("newMessage(): could not decode as Techem Message: {}, original message: {}", e1.getMessage(), message.toString());
				}
			}
			wmBusBridgeHandler.processMessage(device);
			logger.trace("newMessage(): Forwarded to handler.processMessage()");
		} else {
			logger.trace("newMessage(): Unmatched message received: {}", message.toString());
		}
	}

	@Override
	public void discardedBytes(byte[] bytes) {
		logger.debug("Bytes discarded by radio module: {}", HexConverter.bytesToHex(bytes));
	}

	@Override
	public void stoppedListening(IOException e) {
		logger.debug("Stopped listening for new messages. Reason: {}", e.getMessage());
		e.printStackTrace();
	}

}