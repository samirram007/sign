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
    Cipher cipher;

    private static final String AES_MODE = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    public String encrypt(String plainText, String key) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {


    public String encryptForEri(String plainText, SecretKey key) throws Exception {


//        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

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

    public String encryptForEri(String plainText, String base64Key) throws Exception {

        // 1️⃣ Decode AES key from base64
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        // 2️⃣ Generate random IV
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 3️⃣ Encrypt UTF-8 bytes
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        byte[] encrypted = cipher.doFinal(
                plainText.getBytes(StandardCharsets.UTF_8)
        );

        // 4️⃣ Concatenate IV + ciphertext
        byte[] payload = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, payload, 0, iv.length);
        System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);

        // 5️⃣ Base64 encode
        return Base64.getEncoder().encodeToString(payload);
    }

    public String decryptForEri(String encryptedString,SecretKey key)throws Exception{
        cipher = Cipher.getInstance("AES");
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedTextByte = decoder.decode(encryptedString);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
        String decryptedText = new String(decryptedByte);
        return decryptedText;
    }
}
