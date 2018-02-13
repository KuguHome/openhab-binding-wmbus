package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal;

class HexConverter {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
