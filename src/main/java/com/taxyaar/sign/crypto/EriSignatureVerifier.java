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
}
