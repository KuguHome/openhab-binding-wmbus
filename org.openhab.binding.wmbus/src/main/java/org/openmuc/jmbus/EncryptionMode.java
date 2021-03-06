/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

import java.util.HashMap;
import java.util.Map;

/**
 * The encryption modes.
 */
public enum EncryptionMode {
    /**
     * No encryption.
     */
    NONE(0),
    /**
     * AES with Counter Mode (CTR) noPadding and IV.
     */
    AES_128(1),
    /**
     * DES with Cipher Block Chaining Mode (CBC). <br>
     * Not supported yet.
     */
    DES_CBC(2),
    /**
     * DES with Cipher Block Chaining Mode (CBC) and Initial Vector.<br>
     * Not supported yet.
     */
    DES_CBC_IV(3),
    RESERVED_04(4),
    /**
     * AES with Cipher Block Chaining Mode (CBC) and Initial Vector.
     */
    AES_CBC_IV(5),
    RESERVED_06(6),
    /**
     * AES 128 with Cipher Block Chaining Mode (CBC) and dynamic key and Initial Vector with 0.<br>
     * TR-03109-1 Anlage Feinspezifikation Drahtlose LMN Schnittstelle-Teil2<br>
     * Not supported yet.
     */
    AES_CBC_IV_0(7),
    RESERVED_08(8),
    RESERVED_09(9),
    RESERVED_10(10),
    RESERVED_11(11),
    RESERVED_12(12),
    /**
     * TLS 1.2<br>
     * TR-03109-1 Anlage Feinspezifikation Drahtlose LMN Schnittstelle-Teil2<br>
     * Not supported yet.
     */
    TLS(13),
    RESERVED_14(14),
    RESERVED_15(15);

    private final int id;

    private static final Map<Integer, EncryptionMode> idMap = new HashMap<>();

    static {
        for (EncryptionMode enumInstance : EncryptionMode.values()) {
            if (idMap.put(enumInstance.getId(), enumInstance) != null) {
                throw new IllegalArgumentException("duplicate ID: " + enumInstance.getId());
            }
        }
    }

    private EncryptionMode(int id) {
        this.id = id;
    }

    /**
     * Returns the ID of this EncryptionMode.
     * 
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the EncryptionMode that corresponds to the given ID. Throws an IllegalArgumentException if no
     * EncryptionMode with the given ID exists.
     * 
     * @param id
     *            the ID
     * @return the EncryptionMode that corresponds to the given ID
     */
    public static EncryptionMode getInstance(int id) {
        EncryptionMode enumInstance = idMap.get(id);
        if (enumInstance == null) {
            throw new IllegalArgumentException("invalid ID: " + id);
        }
        return enumInstance;
    }
}
