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

    private static final String AES_MODE = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
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

    public String getEncryptedPlainText(String plainText, SecretKey key) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] plainTextByte = plainText.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);
        return Base64.getEncoder().encodeToString(encryptedByte);
    }

//    public String encryptForEri(String plainText, String base64Key) throws Exception {
//
//        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
//        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
//
//        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//
//        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
//
//        return Base64.getEncoder().encodeToString(encrypted);
//    }

    public String getEncryptedPlainText(String plainText, String semetricKey) throws Exception {
        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(semetricKey), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] plainTextByte = plainText.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);
        return Base64.getEncoder().encodeToString(encryptedByte);
    }

//    public String encryptForEri(String plainText, String base64Key) throws Exception {
//
//        // 1️⃣ Decode AES key from base64
//        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
//        SecretKey key = new SecretKeySpec(keyBytes, "AES");
//
//        // 2️⃣ Generate random IV
//        byte[] iv = new byte[IV_LENGTH];
//        SecureRandom random = new SecureRandom();
//        random.nextBytes(iv);
//
//        IvParameterSpec ivSpec = new IvParameterSpec(iv);
//
//        // 3️⃣ Encrypt UTF-8 bytes
//        Cipher cipher = Cipher.getInstance(AES_MODE);
//        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
//
//        byte[] encrypted = cipher.doFinal(
//                plainText.getBytes(StandardCharsets.UTF_8)
//        );
//
//        // 4️⃣ Concatenate IV + ciphertext
//        byte[] payload = new byte[iv.length + encrypted.length];
//        System.arraycopy(iv, 0, payload, 0, iv.length);
//        System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
//
//        // 5️⃣ Base64 encode
//        return Base64.getEncoder().encodeToString(payload);
//    }
}
