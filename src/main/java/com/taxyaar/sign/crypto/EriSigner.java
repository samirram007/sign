package com.taxyaar.sign.crypto;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EriSigner {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EriSigner.class);

    private final PfxKeyLoader keyLoader;

    public EriSigner(PfxKeyLoader keyLoader) {
        this.keyLoader = keyLoader;
    }

//    public byte[] sign(String data) throws Exception {
//        LOGGER.info("Entering generateSign");
//
//        PrivateKey privateKey = keyLoader.loadPrivateKey();
//        X509Certificate certificate = keyLoader.loadCertificate();
//
//        X509CertificateHolder certHolder = new X509CertificateHolder(certificate.getEncoded());
//
//        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
//
//        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
//                .setProvider("BC")
//                .build(privateKey);
//
//        generator.addSignerInfoGenerator(
//                new JcaSignerInfoGeneratorBuilder(
//                        new JcaDigestCalculatorProviderBuilder()
//                                .setProvider("BC")
//                                .build())
//                        .build(signer, certHolder));
//
//        generator.addCertificates(new JcaCertStore(List.of(certificate)));
//
//        CMSTypedData cmsData = new CMSProcessableByteArray(data.getBytes());
//
//        CMSSignedData signedData = generator.generate(cmsData, false);
//
//        LOGGER.info("Exit generateSign");
//        return signedData.getEncoded();
//    }

//    public byte[] sign(String rawJson) throws Exception {
//
//        LOGGER.info("Entering generateSign");
//
//        PrivateKey privateKey = keyLoader.loadPrivateKey();
//        X509Certificate certificate = keyLoader.loadCertificate();
//
//        X509CertificateHolder certHolder =
//                new X509CertificateHolder(certificate.getEncoded());
//
//        CMSSignedDataGenerator generator =
//                new CMSSignedDataGenerator();
//
//        ContentSigner signer =
//                new JcaContentSignerBuilder("SHA256withRSA")
//                        .setProvider("BC")
//                        .build(privateKey);
//
//        generator.addSignerInfoGenerator(
//                new JcaSignerInfoGeneratorBuilder(
//                        new JcaDigestCalculatorProviderBuilder()
//                                .setProvider("BC")
//                                .build())
//                        .build(signer, certHolder));
//
//        generator.addCertificates(
//                new JcaCertStore(List.of(certificate)));
//
//        // ✅ CRITICAL: UTF-8 RAW JSON bytes
//        byte[] rawBytes = rawJson.getBytes(StandardCharsets.UTF_8);
//
//        CMSTypedData cmsData =
//                new CMSProcessableByteArray(rawBytes);
//
//        // ✅ DETACHED signature
//        CMSSignedData signedData =
//                generator.generate(cmsData, false);
//
//        LOGGER.info("Exit generateSign");
//
//        return signedData.getEncoded();
//    }

    public byte[] sign(String rawJsonPayload) throws Exception {

        LOGGER.info("Entering generateSign");

        PrivateKey privateKey = keyLoader.loadPrivateKey();
        X509Certificate certificate = keyLoader.loadCertificate();

        // UTF-8 raw JSON bytes (IMPORTANT)
        byte[] payloadBytes =
                rawJsonPayload.getBytes(StandardCharsets.UTF_8);

        ContentSigner contentSigner =
                new JcaContentSignerBuilder("SHA256withRSA")
                        .setProvider("BC")
                        .build(privateKey);

        CMSSignedDataGenerator generator =
                new CMSSignedDataGenerator();

        generator.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder()
                                .setProvider("BC")
                                .build()
                ).build(contentSigner, certificate)
        );

        // include certificate chain
//        generator.addCertificates(
//                new JcaCertStore(List.of(certificate))
//        );

        generator.addCertificates(
                new JcaCertStore(keyLoader.loadFullChain())
        );

        // 🔥 DETACHED signature
        CMSTypedData cmsData =
                new CMSProcessableByteArray(payloadBytes);

        CMSSignedData signedData =
                generator.generate(cmsData, false);

        LOGGER.info("Exit generateSign");

        return signedData.getEncoded();
    }


    public String sign(byte[] data, PrivateKey key, X509Certificate cert)
            throws Exception {

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

        gen.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder()
                                .setProvider("BC")
                                .build())
                        .build(
                                new JcaContentSignerBuilder("SHA256withRSA")
                                        .setProvider("BC")
                                        .build(key),
                                cert));

        gen.addCertificates(
                new JcaCertStore(java.util.List.of(cert)));

        CMSSignedData signedData = gen.generate(new CMSProcessableByteArray(data), false);

        return Base64.encodeBase64String(signedData.getEncoded());
    }

}
