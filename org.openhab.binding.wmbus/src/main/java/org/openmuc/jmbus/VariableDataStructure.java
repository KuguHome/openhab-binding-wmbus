/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Representation of the data transmitted in RESP-UD (M-Bus) and SND-NR (wM-Bus) messages.
 * 
 * @see #decode()
 */
public class VariableDataStructure {

    private static final ConcurrentHashMap<SecondaryAddress, List<DataRecord>> deviceHistory = new ConcurrentHashMap<>();

    private final byte[] buffer;
    private final int offset;
    private final int length;
    private byte[] header = new byte[0];
    private final SecondaryAddress linkLayerSecondaryAddress;
    private final Map<SecondaryAddress, byte[]> keyMap;

    private SecondaryAddress secondaryAddress;
    private int accessNumber;
    private int status;

    /* Extended Link Layer (ELL) (0x8d) specific */
    private byte communicationControl;
    private byte[] sessionNumber;
    /* End of ELL specific */

    private EncryptionMode encryptionMode;
    private int numberOfEncryptedBlocks;
    private byte[] manufacturerData = new byte[0];
    private byte[] vdr = new byte[0];
    private boolean moreRecordsFollow = false;

    private boolean decoded = false;

    private List<DataRecord> dataRecords;

    private int ciField;

    public VariableDataStructure(byte[] buffer, int offset, int length, SecondaryAddress linkLayerSecondaryAddress,
            Map<SecondaryAddress, byte[]> keyMap) {
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
        this.linkLayerSecondaryAddress = linkLayerSecondaryAddress;
        this.keyMap = keyMap;
        this.dataRecords = new LinkedList<>();
    }

    /**
     * This method is used to
     * 
     * @throws DecodingException
     */
    public void decode() throws DecodingException {
        if (!decoded) {
            try {
                ciField = readUnsignedByte(buffer, offset);

                switch (ciField) {
                    case 0x72:
                        decodeLongHeaderData();
                        break;
                    case 0x78: /* no header */
                        encryptionMode = EncryptionMode.NONE;
                        decodeDataRecords(buffer, offset + 1, length - 1);
                        break;
                    case 0x7a: /* short header */
                        decodeShortHeader();
                        break;
                    case 0x8d: /* Extended Link Layer */
                        decodeExtendedLinkLayer(buffer, offset + 1); // 6 bytes header + CRC
                        header = Arrays.copyOfRange(buffer, offset, offset + 7); // don't include CRC
                        vdr = new byte[length - 7];
                        System.arraycopy(buffer, offset + 7, vdr, 0, length - 7);
                        if (encryptionMode.equals(EncryptionMode.AES_128)) {
                            decryptMessage(getKey());
                        }

                        if ((vdr[2] & 0xff) == 0x78) {
                            decodeDataRecords(vdr, 3, length - 10);
                        } else if ((vdr[2] & 0xff) == 0x79) {
                            decodeShortFrame(vdr, 3, length - 10);
                        }
                        break;
                    case 0x33:
                        String msg = String.format(
                                "Received telegram with CI 0x33. Decoding not implemented. Device Serial: %s, Manufacturer: %s.",
                                linkLayerSecondaryAddress.getDeviceId().toString(),
                                linkLayerSecondaryAddress.getManufacturerId());
                        throw new DecodingException(msg);
                    default:
                        String strFormat = "Unable to decode message with this CI Field: 0x%02X.";
                        if ((ciField >= 0xA0) && (ciField <= 0xB7)) {
                            strFormat = "Manufacturer specific CI: 0x%02X.";
                        }

                        throw new DecodingException(String.format(strFormat, ciField));
                }
            } catch (RuntimeException e) {
                throw new DecodingException(e);
            }
            decoded = true;
        }
    }

    private void decodeShortHeader() throws DecodingException {
        decodeShortHeader(buffer, offset + 1);

        switch (encryptionMode) {
            case NONE:
                decodeDataRecords(buffer, offset + 5, length - 5);
                break;
            case AES_CBC_IV:
                decryptAesCbcIv(buffer, offset + 5, numberOfEncryptedBlocks * 16);
                break;
            case AES_128:
            case AES_CBC_IV_0:
            case DES_CBC:
            case DES_CBC_IV:
            case RESERVED_04:
            case RESERVED_06:
            case RESERVED_08:
            case RESERVED_09:
            case RESERVED_10:
            case RESERVED_11:
            case RESERVED_12:
            case RESERVED_14:
            case RESERVED_15:
            case TLS:
            default:
                throw new DecodingException("Unsupported encryption mode used: " + encryptionMode);
        }
    }

    private void decryptAesCbcIv(byte[] buffer, int offset, int encryptedDataLength) throws DecodingException {
        vdr = new byte[encryptedDataLength];

        System.arraycopy(buffer, offset, vdr, 0, encryptedDataLength);

        byte[] key = keyMap.get(linkLayerSecondaryAddress);
        if (key == null) {
            String msg = MessageFormat.format(
                    "Unable to decode encrypted payload. \nSecondary address key was not registered: \n{0}",
                    linkLayerSecondaryAddress);
            throw new DecodingException(msg);
        }

        decodeDataRecords(decryptMessage(key), 0, encryptedDataLength);
    }

    private void decodeLongHeaderData() throws DecodingException {
        final int headerLength = 13;
        header = Arrays.copyOfRange(buffer, offset, offset + headerLength);

        secondaryAddress = SecondaryAddress.newFromLongHeader(buffer, offset + 1);

        decodeShortHeader(buffer, offset + 1 + 8);

        vdr = new byte[length - headerLength];
        System.arraycopy(buffer, offset + headerLength, vdr, 0, length - headerLength);

        switch (encryptionMode) {
            case NONE:
                // nothing to do
                break;
            case AES_CBC_IV:
                decryptMessage(getKey());
                break;
            case AES_128:
            case AES_CBC_IV_0:
            case DES_CBC:
            case DES_CBC_IV:
            case RESERVED_04:
            case RESERVED_06:
            case RESERVED_08:
            case RESERVED_09:
            case RESERVED_10:
            case RESERVED_11:
            case RESERVED_12:
            case RESERVED_14:
            case RESERVED_15:
            case TLS:
            default:
                throw new DecodingException("Unsupported encryption mode used: " + encryptionMode);
        }
        decodeDataRecords(vdr, 0, length - headerLength);
    }

    public SecondaryAddress getSecondaryAddress() {
        return secondaryAddress;
    }

    public int getAccessNumber() {
        return accessNumber;
    }

    public EncryptionMode getEncryptionMode() {
        return encryptionMode;
    }

    public byte[] getManufacturerData() {
        return manufacturerData;
    }

    public int getNumberOfEncryptedBlocks() {
        return numberOfEncryptedBlocks;
    }

    public int getStatus() {
        return status;
    }

    public List<DataRecord> getDataRecords() {
        return dataRecords;
    }

    public boolean moreRecordsFollow() {
        return moreRecordsFollow;
    }

    private void decodeExtendedLinkLayer(byte[] buffer, int offset) {
        int i = offset;

        communicationControl = buffer[i++];
        accessNumber = buffer[i++];
        sessionNumber = new byte[] { buffer[i++], buffer[i++], buffer[i++], buffer[i++] };
        encryptionMode = EncryptionMode.getInstance(sessionNumber[3] >> 5);
        byte[] checksum = new byte[] { buffer[i++], buffer[i++] };

        byte[] crc = CRC16.calculateCrc16(Arrays.copyOfRange(buffer, i, buffer.length - 1));
        if (checksum[0] == crc[0] && checksum[1] == crc[1]) {
            encryptionMode = EncryptionMode.NONE;
        }
    }

    private void decodeShortHeader(byte[] buffer, int offset) {
        int i = offset;

        accessNumber = readUnsignedByte(buffer, i++);
        status = readUnsignedByte(buffer, i++);
        numberOfEncryptedBlocks = (buffer[i++] & 0xf0) >> 4;
        encryptionMode = EncryptionMode.getInstance(buffer[i++] & 0x0f);

        if (msgIsNotEnc(buffer, i) || numberOfEncryptedBlocks == 0) {
            encryptionMode = EncryptionMode.NONE;
        }
    }

    private static boolean msgIsNotEnc(byte[] buffer, int i) {
        byte b = buffer[i];
        byte b2 = buffer[i + 1];
        return b == (byte) 0x2f && b2 == (byte) 0x02;
    }

    private static int readUnsignedByte(byte[] msg, int i) {
        return msg[i] & 0xff;
    }

    public byte[] getHeader() {
        return this.header;
    }

    private void decodeDataRecords(byte[] buffer, int offset, int length) throws DecodingException {
        int i = offset;

        while (i < offset + length - 2) {

            if ((buffer[i] & 0xef) == 0x0f) {
                // manufacturer specific data

                moreRecordsFollow = (buffer[i] & 0x10) == 0x10;

                manufacturerData = Arrays.copyOfRange(buffer, i + 1, offset + length - 2);
                return;
            }

            if (buffer[i] == 0x2f) {
                // this is a fill byte because some encryption mechanisms need multiples of 8 bytes to encode data
                i++;
                continue;
            }

            DataRecord dataRecord = new DataRecord();
            i = dataRecord.decode(buffer, i);

            dataRecords.add(dataRecord);
        }

        if (linkLayerSecondaryAddress != null) {
            deviceHistory.put(linkLayerSecondaryAddress, dataRecords);
        }
    }

    private void decodeShortFrame(byte[] data, int offset, int length) throws DecodingException {
        if (!deviceHistory.containsKey(linkLayerSecondaryAddress)) {
            deviceHistory.put(linkLayerSecondaryAddress, new LinkedList<DataRecord>());
        }

        ByteBuffer buf = ByteBuffer.wrap(data, offset, length);
        buf.order(ByteOrder.nativeOrder());

        // skip checksum data
        buf.position(4);

        this.dataRecords = deviceHistory.get(linkLayerSecondaryAddress);

        ListIterator<DataRecord> iter = this.dataRecords.listIterator();
        while (iter.hasNext()) {
            DataRecord dr = iter.next();
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                os.write(dr.getDib());
                os.write(dr.getVib());

                int tempOffset = dr.getDib().length + dr.getVib().length;

                // This might not be right
                int dataLegth = tempOffset + dr.getDataLength();
                byte[] b = new byte[dataLegth - tempOffset];
                buf.get(b);
                os.write(b);

                DataRecord newDataRecord = new DataRecord();
                newDataRecord.decode(os.toByteArray(), 0);
                iter.set(newDataRecord);
            } catch (IOException e) {
                // ignore
            }

        }
    }

    public byte[] decryptMessage(byte[] key) throws DecodingException {

        if (encryptionMode == EncryptionMode.NONE) {
            return vdr;
        }

        if (key == null) {
            throw new DecodingException("AES key for given address not specified.");
        }

        final int len = numberOfEncryptedBlocks * 16;

        if (len > vdr.length) {
            throw new DecodingException("Number of encrypted exceeds payload size!");
        }

        switch (encryptionMode) {
            case AES_CBC_IV:
                decryptAesCbcIv(key, len);
                break;
            case AES_128:
                decryptAes128(key, len);
                break;
            default:
                throw new DecodingException("Unsupported encryption mode: " + encryptionMode);
        }

        return vdr;
    }

    private void decryptAes128(byte[] key, final int len) throws DecodingException {
        byte[] iv = createIvKamstrup();
        byte[] result = AesCrypt.newAesCtrCrypt(key, iv).decrypt(vdr, len);

        byte[] crc = CRC16.calculateCrc16(Arrays.copyOfRange(result, 2, result.length));

        if (result[0] != crc[0] || result[1] != crc[1]) {
            throw new DecodingException(newDecyptionExceptionMsg());
        }
        vdr = result;
    }

    private void decryptAesCbcIv(byte[] key, final int len) throws DecodingException {
        byte[] iv = createIv();
        byte[] result = AesCrypt.newAesCrypt(key, iv).decrypt(this.vdr, len);

        if (!(result[0] == 0x2f && result[1] == 0x2f)) {
            throw new DecodingException(newDecyptionExceptionMsg());
        }
        System.arraycopy(result, 0, vdr, 0, len);
    }

    private String newDecyptionExceptionMsg() {
        String deviceId = linkLayerSecondaryAddress.getDeviceId().toString();
        String manId = linkLayerSecondaryAddress.getManufacturerId();
        return String.format("%s - %s - Decryption unsuccessful! Wrong AES/CTR Key?", deviceId, manId);
    }

    private byte[] createIv() {
        byte[] iv = new byte[16];
        byte[] saBytes = linkLayerSecondaryAddress.asByteArray();

        if (linkLayerSecondaryAddress.isLongHeader()) {
            System.arraycopy(saBytes, 0, iv, 4, 2); // Manufacture
            System.arraycopy(saBytes, 2, iv, 0, 4); // Identification
            System.arraycopy(saBytes, 6, iv, 6, 2); // Version and Device Type
        } else if (ciField == 0x72) {
            saBytes = secondaryAddress.asByteArray();
            System.arraycopy(saBytes, 0, iv, 2, 4); // Identification
            System.arraycopy(saBytes, 4, iv, 0, 2); // Manufacture
            System.arraycopy(saBytes, 6, iv, 6, 2); // Version and Device Type
        } else {
            System.arraycopy(saBytes, 0, iv, 0, 8);
        }

        for (int i = 8; i < iv.length; i++) {
            iv[i] = (byte) accessNumber;
        }

        return iv;
    }

    private byte[] createIvKamstrup() throws DecodingException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            os.write(linkLayerSecondaryAddress.asByteArray(), 0, 8);
            /* set hop count to 0 in case a repeater is used */
            os.write(communicationControl & ~(1 << 4));
            os.write(sessionNumber);
            os.write(new byte[3]); // 3 * 0x00

            return os.toByteArray();
        } catch (IOException e) {
            throw new DecodingException("Unable to create initial vector for decryption.", e);
        }
    }

    private byte[] getKey() throws DecodingException {
        byte[] key = keyMap.get(linkLayerSecondaryAddress);
        if (key != null) {
            return key;
        }

        String msg = "Unable to decode encrypted payload because no key for the following secondary address was registered: "
                + linkLayerSecondaryAddress;
        throw new DecodingException(msg);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (!decoded) {
            if (dataRecords.isEmpty()) {
                int from = offset;
                int to = from + length;
                String hexString = HexUtils.bytesToHex(Arrays.copyOfRange(buffer, from, to));
                return MessageFormat.format("VariableDataResponse has not been decoded. Bytes:\n{0}", hexString);
            } else {
                builder.append("VariableDataResponse has not been fully decoded. " + dataRecords.size()
                        + " data records decoded.\n");
            }
        }

        if (secondaryAddress != null) {
            builder.append("Secondary address: {").append(secondaryAddress).append("}\n");
        }

        builder.append("Short Header: {Access No.: ").append(accessNumber).append(", status: ").append(status)
                .append(", encryption mode: ").append(encryptionMode).append(", number of encrypted blocks: ")
                .append(numberOfEncryptedBlocks).append("}");

        for (DataRecord dataRecord : dataRecords) {
            builder.append("\n").append(dataRecord.toString());
        }

        if (manufacturerData.length != 0) {
            String manDaraHexStr = HexUtils.bytesToHex(manufacturerData);
            builder.append("\nManufacturer specific bytes:\n").append(manDaraHexStr);
        }

        if (moreRecordsFollow) {
            builder.append("\nMore records follow ...");
        }
        return builder.toString();
    }
}
