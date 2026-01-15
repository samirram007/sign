package com.taxyaar.sign.crypto;

import java.security.cert.X509Certificate;
import java.util.Collection;

import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Component;

@Component
public class EriSignatureVerifier {

    public boolean verify(String sign, String data, X509Certificate cert) throws Exception {

        CMSTypedData cmsData = new CMSProcessableByteArray(Base64.decode(data));

        CMSSignedData cms = new CMSSignedData(cmsData, Base64.decode(sign));

        SignerInformationStore signers = cms.getSignerInfos();
        Collection<SignerInformation> infos = signers.getSigners();

        for (SignerInformation signer : infos) {
            return signer.verify(
                    new JcaSimpleSignerInfoVerifierBuilder()
                            .setProvider("BC")
                            .build(cert));
        }
        return false;
    }

    public boolean verifySignedData(
            String signBase64,
            String dataBase64,
            X509Certificate cert) throws Exception {

        byte[] signBytes = java.util.Base64.getDecoder().decode(signBase64);
        byte[] dataBytes = java.util.Base64.getDecoder().decode(dataBase64);

        CMSSignedData cms = new CMSSignedData(
                new CMSProcessableByteArray(dataBytes),
                signBytes);

        for (SignerInformation signer : cms.getSignerInfos().getSigners()) {
            return signer.verify(
                    new JcaSimpleSignerInfoVerifierBuilder()
                            .setProvider("BC")
                            .build(cert));
        }
        return false;
    }

}
