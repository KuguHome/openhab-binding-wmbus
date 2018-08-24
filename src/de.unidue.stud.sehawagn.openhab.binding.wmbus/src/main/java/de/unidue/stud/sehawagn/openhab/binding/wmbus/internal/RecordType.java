package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal;

import java.util.Arrays;

import org.openmuc.jmbus.DataRecord;

public class RecordType {

	private final byte[] dib;
	private final byte[] vib;

	public RecordType(byte[] dib, byte[] vib) {
		this.dib = dib;
		this.vib = vib;
	}

	public RecordType(int dib, int vib) {
		this(new byte[] { (byte) dib }, new byte[] { (byte) vib });
	}

	public byte[] getDib() {
		return dib;
	}

	public byte[] getVib() {
		return vib;
	}

	boolean matches(DataRecord record) {
		return Arrays.equals(record.getDib(), getDib()) && Arrays.equals(record.getVib(), getVib());
	}

}
