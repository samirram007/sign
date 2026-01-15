package com.taxyaar.sign.crypto;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class CerCertificateLoader {

    private final ResourceLoader resourceLoader;

    public CerCertificateLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public X509Certificate loadCertificate(String cerPath) throws Exception {

        Resource resource = resourceLoader.getResource(cerPath);

        if (!resource.exists()) {
            throw new IllegalStateException("CER file not found: " + cerPath);
        }

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        try (var is = resource.getInputStream()) {
            return (X509Certificate) cf.generateCertificate(is);
        }
    }
}
