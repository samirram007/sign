package com.taxyaar.sign.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

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

    public byte[] sign(byte[] rawBytes) throws Exception {
        return signer.sign(rawBytes);
    }

//    public SignedDataResponseDto generate(SignedDataRequestDto req) throws Exception {
//
//        String rawJson = req.getDataToSign();
//
//        System.out.println("===== SIGN INPUT (RAW JSON) =====");
//        System.out.println(rawJson);
//        System.out.println("================================");
//
//        // ✔ sign RAW JSON bytes
//        byte[] signedBytes = signer.sign(rawJson);
//
//        // ✔ base64 JSON for transport
//        String base64Data = Base64.encodeBase64String(
//                rawJson.getBytes(StandardCharsets.UTF_8));
//
//        SignedDataResponseDto res = new SignedDataResponseDto();
//        res.setSign(Base64.encodeBase64String(signedBytes));
//        res.setData(base64Data);
//        res.setEriUserId(req.getEriUserId());
//
//        System.out.println("===== BASE64 DATA =====");
//        System.out.println(base64Data);
//        System.out.println("=======================");
//
//        return res;
//    }



    public SignedDataResponseDto generate(SignedDataRequestDto req) throws Exception {

        byte[] signedBytes = signer.sign(req.getDataToSign());

        SignedDataResponseDto res = new SignedDataResponseDto();
        res.setSign(Base64.encodeBase64String(signedBytes));
        res.setData(Base64.encodeBase64String(req.getDataToSign().getBytes()));
        System.out.println("Singed User ID: "+req.getEriUserId());
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


        // return cryptoUtil.encrypt(plainText, key);
        try {
            // return cryptoUtil.encrypt(plainText, key);
            return cryptoUtil.getEncryptedPlainText(plainText, key);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new Exception("Error encrypting plain text: " + e.getMessage(), e);
        }
    }
}