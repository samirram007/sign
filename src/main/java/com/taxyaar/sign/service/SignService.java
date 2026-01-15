package com.taxyaar.sign.service;

import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taxyaar.sign.crypto.CerCertificateLoader;
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

    @Value("${eri.cer.path}")
    private String cerPath;

    public SignService(EriSigner signer, PfxKeyLoader loader, CerCertificateLoader certLoader,
            EriSignatureVerifier verifier) {
        this.signer = signer;
        this.loader = loader;
        this.certLoader = certLoader;
        this.verifier = verifier;
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
}
