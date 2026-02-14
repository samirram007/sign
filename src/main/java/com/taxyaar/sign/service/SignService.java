package com.taxyaar.sign.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taxyaar.sign.crypto.CerCertificateLoader;
import com.taxyaar.sign.crypto.CryptoUtil;
import com.taxyaar.sign.crypto.EriSignatureVerifier;
import com.taxyaar.sign.crypto.EriSigner;
import com.taxyaar.sign.crypto.PfxKeyLoader;
import com.taxyaar.sign.dto.request.SignedDataRequestDto;
import com.taxyaar.sign.dto.request.VerifySignRequestDto;
import com.taxyaar.sign.dto.response.SignedDataResponseDto;

@Service
public class SignService {

    private final EriSigner signer;
    private final PfxKeyLoader loader;
    private final EriSignatureVerifier verifier;
    private final CerCertificateLoader certLoader;
    private final CryptoUtil cryptoUtil;

    @Value("${eri.cer.path}")
    private String cerPath;

    public SignService(EriSigner signer, PfxKeyLoader loader, CerCertificateLoader certLoader,
            EriSignatureVerifier verifier, CryptoUtil cryptoUtil) {
        this.signer = signer;
        this.loader = loader;
        this.certLoader = certLoader;
        this.verifier = verifier;
        this.cryptoUtil = cryptoUtil;
    }

    public SignedDataResponseDto generate(SignedDataRequestDto req) throws Exception {

        byte[] signedBytes = signer.sign(req.getDataToSign());

        SignedDataResponseDto res = new SignedDataResponseDto();
        res.setSign(Base64.encodeBase64String(signedBytes));
        res.setData(Base64.encodeBase64String(req.getDataToSign().getBytes()));
        res.setEriUserId(req.getEriUserId());

        return res;
    }

    public boolean verify(VerifySignRequestDto req) throws Exception {
        return new EriSignatureVerifier().verify(
                req.getSign(),
                req.getData(),
                loader.loadCertificate());
    }

    public boolean verifyCert(VerifySignRequestDto request) throws Exception {

        X509Certificate cert = certLoader.loadCertificate(cerPath);

        return verifier.verifySignedData(
                request.getSign(),
                request.getData(),
                cert);
    }

    public String getEncryptedPlainText(String plainText, String key) throws Exception {



        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(key);

            SecretKey secretKey=new SecretKeySpec(keyBytes, "AES");
            return cryptoUtil.encryptForEri(plainText, secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new Exception("Error encrypting plain text: " + e.getMessage(), e);
        }
    }

    public String getDecryptedPlainText(String encryptedString, String key) throws
            Exception {

        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(key);

            SecretKey secretKey=new SecretKeySpec(keyBytes, "AES");
            return cryptoUtil.decryptForEri(encryptedString, secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new Exception("Error encrypting plain text: " + e.getMessage(), e);
        }
    }
}