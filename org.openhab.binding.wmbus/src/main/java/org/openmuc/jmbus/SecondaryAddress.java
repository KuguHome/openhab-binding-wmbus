/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * This class represents a secondary address. Use the static initializer to initialize the
 */
public class SecondaryAddress implements Comparable<SecondaryAddress> {

    private static final int SECONDARY_ADDRESS_LENGTH = 8;

    private static final int ID_NUMBER_LENGTH = 4;

    private final String manufacturerId;
    private final Bcd deviceId;
    private final int version;
    private final DeviceType deviceType;
    private final byte[] bytes;
    private final int hashCode;
    private final boolean isLongHeader;

    /**
     * Instantiate a new secondary address within a long header.
     * 
     * @param buffer
     *            the byte buffer.
     * @param offset
     *            the offset.
     * @return a new secondary address.
     */
    public static SecondaryAddress newFromLongHeader(byte[] buffer, int offset) {
        return new SecondaryAddress(buffer, offset, true);
    }

    /**
     * Instantiate a new secondary address within a wireless M-Bus link layer header.
     * 
     * @param buffer
     *            the byte buffer.
     * @param offset
     *            the offset.
     * @return a new secondary address.
     */
    public static SecondaryAddress newFromWMBusHeader(byte[] buffer, int offset) {
        return new SecondaryAddress(buffer, offset, false);
    }

    /**
     * Instantiate a new secondary address for a manufacturer ID.
     * 
     * @param idNumber
     *            ID number.
     * @param manufactureId
     *            manufacturer ID.
     * @param version
     *            the version.
     * @param media
     *            the media.
     * @return a new secondary address.
     * @throws NumberFormatException
     *             if the idNumber is not long enough.
     */
    public static SecondaryAddress newFromManufactureId(byte[] idNumber, String manufactureId, byte version, byte media,
            boolean longHeader) throws NumberFormatException {
        if (idNumber.length != ID_NUMBER_LENGTH) {
            throw new NumberFormatException("Wrong length of ID. Length must be " + ID_NUMBER_LENGTH + " byte.");
        }

        byte[] mfId = encodeManufacturerId(manufactureId);

        ByteBuffer byteBuffer = ByteBuffer.allocate(idNumber.length + mfId.length + 1 + 1);
        if (longHeader) {
            byteBuffer.put(idNumber).put(mfId);
        } else {
            byteBuffer.put(mfId).put(idNumber);
        }
        byte[] buffer = byteBuffer.put(version).put(media).array();

        return new SecondaryAddress(buffer, 0, true);
    }

    /**
     * The {@link SecondaryAddress} as byte array.
     * 
     * @return the byte array (octet string) representation.
     */
    public byte[] asByteArray() {
        return bytes;
    }

    /**
     * Get the manufacturer ID.
     * 
     * @return the ID.
     */
    public String getManufacturerId() {
        return manufacturerId;
    }

    /**
     * Returns the device ID. This is secondary address of the device.
     * 
     * @return the device ID
     */
    public Bcd getDeviceId() {
        return deviceId;
    }

    /**
     * Returns the device type (e.g. gas, water etc.)
     * 
     * @return the device type
     */
    public DeviceType getDeviceType() {
        return deviceType;
    }

    /**
     * Get the version.
     * 
     * @return the version.
     */
    public int getVersion() {
        return version;
    }

    public boolean isLongHeader() {
        return isLongHeader;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("manufacturer ID: ").append(manufacturerId).append(", device ID: ")
                .append(deviceId).append(", device version: ").append(version).append(", device type: ")
                .append(deviceType).append(", as bytes: ").append(HexUtils.bytesToHex(bytes)).toString();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SecondaryAddress)) {
            return false;
        }

        SecondaryAddress other = (SecondaryAddress) obj;

        return Arrays.equals(this.bytes, other.bytes);
    }

    @Override
    public int compareTo(SecondaryAddress sa) {
        return Integer.compare(hashCode(), sa.hashCode());
    }

    private SecondaryAddress(byte[] buffer, int offset, boolean longHeader) {
        this.bytes = Arrays.copyOfRange(buffer, offset, offset + SECONDARY_ADDRESS_LENGTH);

        this.hashCode = Arrays.hashCode(this.bytes);
        this.isLongHeader = longHeader;

        try (ByteArrayInputStream is = new ByteArrayInputStream(this.bytes)) {
            if (longHeader) {
                this.deviceId = decodeDeviceId(is);
                this.manufacturerId = decodeManufacturerId(is);
            } else {
                this.manufacturerId = decodeManufacturerId(is);
                this.deviceId = decodeDeviceId(is);
            }
            this.version = is.read() & 0xff;
            this.deviceType = DeviceType.getInstance(is.read() & 0xff);
        } catch (IOException e) {
            // should not occur
            throw new RuntimeException(e);
        }
    }

    private static String decodeManufacturerId(ByteArrayInputStream is) {
        int manufacturerIdAsInt = (is.read() & 0xff) + (is.read() << 8);
        char c = (char) ((manufacturerIdAsInt & 0x1f) + 64);
        manufacturerIdAsInt = (manufacturerIdAsInt >> 5);
        char c1 = (char) ((manufacturerIdAsInt & 0x1f) + 64);
        manufacturerIdAsInt = (manufacturerIdAsInt >> 5);
        char c2 = (char) ((manufacturerIdAsInt & 0x1f) + 64);

        return new StringBuilder().append(c2).append(c1).append(c).toString();
    }

    private static byte[] encodeManufacturerId(String manufactureId) {
        if (manufactureId.length() != 3) {
            return new byte[] { 0, 0 };
        }

        manufactureId = manufactureId.toUpperCase();
        char[] manufactureIdArray = manufactureId.toCharArray();
        int manufacturerIdAsInt = (manufactureIdArray[0] - 64) * 32 * 32;
        manufacturerIdAsInt += (manufactureIdArray[1] - 64) * 32;
        manufacturerIdAsInt += (manufactureIdArray[2] - 64);

        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort((short) manufacturerIdAsInt);

        return buf.array();
    }

    private static Bcd decodeDeviceId(ByteArrayInputStream is) throws IOException {
        int msgSize = 4;
        byte[] idArray = new byte[msgSize];
        int actual = is.read(idArray);

        if (msgSize != actual) {
            throw new IOException("Failed to read BCD data. Data missing.");
        }
        return new Bcd(idArray);
    }
}
