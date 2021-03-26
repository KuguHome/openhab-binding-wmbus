/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class AesCrypt {
    private final byte[] key;
    private final byte[] iv;

    protected final SecretKeySpec skeySpec;
    protected final AlgorithmParameterSpec paramSpec;
    protected Cipher cipher;

    public static AesCrypt newAesCrypt(byte[] key, byte[] iv) throws DecodingException {
        return new AesCrypt(key, iv, "AES/CBC/NoPadding");
    }

    public static AesCrypt newAesCtrCrypt(byte[] key, byte[] iv) throws DecodingException {
        return new AesCrypt(key, iv, "AES/CTR/NoPadding");
    }

    private AesCrypt(byte[] key, byte[] iv, String cipherName) throws DecodingException {
        try {
            this.cipher = Cipher.getInstance(cipherName);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new DecodingException(e);
        }

        this.key = Arrays.copyOf(key, key.length);
        this.iv = Arrays.copyOf(iv, iv.length);
        this.skeySpec = new SecretKeySpec(this.key, "AES");
        this.paramSpec = new IvParameterSpec(this.iv);
    }

    public byte[] encrypt(byte[] rawData, int length) throws GeneralSecurityException {
        byte[] tempData = Arrays.copyOf(rawData, length);

        this.cipher.init(Cipher.ENCRYPT_MODE, skeySpec, paramSpec);
        return this.cipher.doFinal(tempData);
    }

    public byte[] decrypt(byte[] rawData, int length) throws DecodingException {
        byte[] encrypted = rawData;

        if (length != 0) {
            encrypted = Arrays.copyOf(rawData, length);
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, paramSpec);
            return cipher.doFinal(encrypted);
        } catch (GeneralSecurityException e) {
            throw new DecodingException(e);
        }
    }
}
