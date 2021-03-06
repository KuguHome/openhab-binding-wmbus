/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

import java.nio.ByteBuffer;

/**
 * 16 bit cyclic redundancy check implementation.
 */
class CRC16 {

    private static byte[] computeCrc(byte[] bytes, int poly, int initialValue, int xorValue) {
        int i;
        int crcVal = initialValue;
        byte[] crc = new byte[2];

        for (byte b : bytes) {
            for (i = 0x80; i != 0; i >>= 1) {
                if ((crcVal & 0x8000) != 0) {
                    crcVal = (crcVal << 1) ^ poly;
                } else {
                    crcVal = crcVal << 1;
                }
                if ((b & i) != 0) {
                    crcVal ^= poly;
                }
            }
        }

        byte[] tmpCrc = ByteBuffer.allocate(4).putInt(Integer.reverseBytes(crcVal & 0xffff ^ xorValue)).array();
        crc[0] = tmpCrc[0];
        crc[1] = tmpCrc[1];

        return crc;
    }

    /**
     * Computes the CRC16 according EN13757.
     * 
     * @param bytes
     *            the data to be checked.
     * @return the CRC16 result.
     */
    public static byte[] calculateCrc16(byte[] bytes) {
        return computeCrc(bytes, 0x3D65, 0x0000, 0xFFFF);
    }

    /**
     * Do not let this class be instantiated.
     */
    private CRC16() {
    }
}
