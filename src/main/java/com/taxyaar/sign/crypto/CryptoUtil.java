package com.taxyaar.sign.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {

    public String encrypt(String plainText, String key) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {

        // Generate random 16-byte IV
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, fromBase64(key), ivSpec);

        byte[] encrypted = cipher.doFinal(
                plainText.getBytes(StandardCharsets.UTF_8));

        // IV + CipherText (very important)
        byte[] ivAndCipherText = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, ivAndCipherText, 0, iv.length);
        System.arraycopy(encrypted, 0, ivAndCipherText, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(ivAndCipherText);
    }

    public String decrypt(String encryptedData, String key) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {

        byte[] ivAndCipherText = Base64.getDecoder().decode(encryptedData);

        // Extract IV
        byte[] iv = new byte[16];
        System.arraycopy(ivAndCipherText, 0, iv, 0, iv.length);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Extract CipherText
        int cipherTextLength = ivAndCipherText.length - iv.length;
        byte[] cipherText = new byte[cipherTextLength];
        System.arraycopy(ivAndCipherText, iv.length, cipherText, 0, cipherTextLength);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, fromBase64(key), ivSpec);

        byte[] decrypted = cipher.doFinal(cipherText);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static SecretKey fromBase64(String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
