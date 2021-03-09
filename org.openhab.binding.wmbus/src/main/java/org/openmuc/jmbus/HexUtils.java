package org.openmuc.jmbus;

public class HexUtils {

    private static final String HEXES = "0123456789ABCDEF";

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * bytes.length);
        for (final byte b : bytes) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static byte[] hexToBytes(String hexString) {
        byte[] bytes = new byte[hexString.length() / 2];
        int index;

        for (int i = 0; i < bytes.length; i++) {
            index = i * 2;
            bytes[i] = (byte) Integer.parseInt(hexString.substring(index, index + 2), 16);
        }
        return bytes;
    }

    private HexUtils() {
        // hide it
    }
}
