package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal;

import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.WMBusMessage;

public class WMBusDevice {

	private WMBusMessage originalMessage;

	public WMBusDevice(WMBusMessage originalMessage) {
		this.originalMessage = originalMessage;
	}

	public WMBusMessage getOriginalMessage() {
		return originalMessage;
	}

	public void decode() throws DecodingException {
		originalMessage.getVariableDataResponse().decode();
	}

	public String getDeviceId() {
		return originalMessage.getSecondaryAddress().getDeviceId().toString();
	}

	public DataRecord findRecord(RecordType recordType) {
		for (DataRecord record : getOriginalMessage().getVariableDataResponse().getDataRecords()) {
			if (recordType.matches(record)) {
				return record;
			}
		}
		return null;
	}

	public DataRecord findRecord(byte[] dib, byte[] vib) {
		return findRecord(new RecordType(dib, vib));
	}
}
