package com.taxyaar.sign.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxyaar.sign.config.TaxConfig;
import com.taxyaar.sign.crypto.CryptoUtil;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TestController {

    private static final Logger log =
            LoggerFactory.getLogger(TestController.class);

    @Autowired
    private CryptoUtil cryptoUtil;

    @Autowired
    private TaxConfig taxConfig;

    @Autowired
    private ResourceLoader resourceLoader;

    private final RestTemplate restTemplate =
            new RestTemplate();

    @PostMapping("/loginnn")
    public String login() throws Exception {

        // 1️⃣ Build payload
        String rawJson = preparePayload();

        Files.writeString(
                Path.of("/var/www/backend/payload.json"),
                rawJson
        );

        // 2️⃣ Sign payload
        byte[] signature = generateSign(rawJson);

        Files.write(
                Path.of("/var/www/backend/sign.p7s"),
                signature
        );

        String base64Data =
                Base64.getEncoder()
                        .encodeToString(rawJson.getBytes(StandardCharsets.UTF_8));

        String base64Sign =
                Base64.getEncoder()
                        .encodeToString(signature);

        // 3️⃣ Build ERI request body
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("data", base64Data);
        request.put("sign", base64Sign);
        request.put("eriUserId", taxConfig.getEriUserId());

        ObjectMapper mapper = new ObjectMapper();
        String finalJson = mapper.writeValueAsString(request);

        log.info("FINAL REQUEST = {}", finalJson);

        // 4️⃣ Send ERI API call
        return sendLogin(finalJson);
    }

    private byte[] generateSign(String data) throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        String pfxCred = taxConfig.getEriPfxPassword();
        String pfxType = taxConfig.getEriPfxType();
        String alias   = taxConfig.getEriPfxAlias();

        KeyStore keyStore =
                KeyStore.getInstance(pfxType, "BC");

        Resource resource =
                resourceLoader.getResource(taxConfig.getEriPfxPath());

        try (InputStream is = resource.getInputStream()) {
            keyStore.load(is, pfxCred.toCharArray());
        }

        Certificate[] chain =
                keyStore.getCertificateChain(alias);

        List<Certificate> certList =
                Arrays.asList(chain);

        JcaCertStore certStore =
                new JcaCertStore(certList);

        PrivateKey privateKey =
                ((KeyStore.PrivateKeyEntry)
                        keyStore.getEntry(alias,
                                new KeyStore.PasswordProtection(
                                        pfxCred.toCharArray())))
                        .getPrivateKey();

        X509Certificate cert =
                (X509Certificate) keyStore.getCertificate(alias);

        X509CertificateHolder holder =
                new X509CertificateHolder(cert.getEncoded());

        CMSSignedDataGenerator gen =
                new CMSSignedDataGenerator();

        ContentSigner signer =
                new JcaContentSignerBuilder("SHA256withRSA")
                        .setProvider("BC")
                        .build(privateKey);

        gen.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder()
                                .setProvider("BC")
                                .build())
                        .build(signer, holder)
        );

        gen.addCertificates(certStore);

        CMSTypedData cmsData =
                new CMSProcessableByteArray(
                        data.getBytes(StandardCharsets.UTF_8));

        // DETACHED signature
        CMSSignedData signed =
                gen.generate(cmsData, false);

        return signed.getEncoded();
    }

    private String preparePayload() throws Exception {

        String pass =
                cryptoUtil.getEncryptedPlainText(
                        taxConfig.getEriPlainText(),
                        taxConfig.getEriPasswordKey());
//
//        DateTimeFormatter fmt =
//                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
//
//        String timeStamp =
//                OffsetDateTime.now(ZoneId.of("Asia/Kolkata"))
//                        .format(fmt);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        .withZone(ZoneId.of("Asia/Kolkata"));

        String timeStamp = formatter.format(Instant.now());

        System.out.println("Asia Time: "+timeStamp);

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put("serviceName",
                taxConfig.getEriServiceName());
        payload.put("entity",
                taxConfig.getEriUserId());
        payload.put("pass", pass);
        payload.put("timeStamp", timeStamp);

        return new ObjectMapper()
                .writeValueAsString(payload);
    }

    private String sendLogin(String body) {

        String url =
                "https://uatocpservices.incometax.gov.in/iec-uat/uat/eriapi/login";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        headers.set("clientId", taxConfig.getEriClientId());
        headers.set("clientSecret", taxConfig.getEriClientSecret());
        headers.set("accessMode", taxConfig.getEriAccessMode());

        headers.set("User-Agent", "TaxYaar-ERI-Client/1.0");
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        headers.set("Host", "uatocpservices.incometax.gov.in");

        HttpEntity<String> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        String.class);

        log.info("ERI RESPONSE = {}", response.getBody());

        return response.getBody();
    }
}
