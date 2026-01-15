package com.taxyaar.sign.service;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

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

    public SignService(EriSigner signer, PfxKeyLoader loader) {
        this.signer = signer;
        this.loader = loader;
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
}
