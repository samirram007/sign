package com.taxyaar.sign.crypto;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import com.taxyaar.sign.config.PfxProperties;

@Component
public class PfxKeyLoader {

    private final PfxProperties props;

    private final ResourceLoader loader;

    public PfxKeyLoader(PfxProperties props, ResourceLoader loader) {
        this.props = props;
        this.loader = loader;
    }

    public PrivateKey loadPrivateKey() throws Exception {
        KeyStore ks = loadKeyStore();
        return ((KeyStore.PrivateKeyEntry) ks.getEntry(
                props.getAlias(),
                new KeyStore.PasswordProtection(props.getPassword().toCharArray()))).getPrivateKey();
    }

    public X509Certificate loadCertificate() throws Exception {
        KeyStore ks = loadKeyStore();
        return (X509Certificate) ks.getCertificate(props.getAlias());
    }

    // private KeyStore loadKeyStore() throws Exception {
    // KeyStore keyStore = KeyStore.getInstance(props.getType(), "BC");
    // InputStream is = new ClassPathResource("cert/eri.pfx").getInputStream();
    // keyStore.load(is, props.getPassword().toCharArray());
    // return keyStore;
    // }

    private KeyStore loadKeyStore() throws Exception {
        Resource resource = loader.getResource(props.getPath());
        KeyStore ks = KeyStore.getInstance(props.getType());
        ks.load(resource.getInputStream(), props.getPassword().toCharArray());
        return ks;
    }

    public List<X509Certificate> loadFullChain() throws Exception {

        KeyStore ks = loadKeyStore();

        Certificate[] chain =
                ks.getCertificateChain(props.getAlias());

        List<X509Certificate> certs = new ArrayList<>();

        if (chain != null) {
            for (Certificate cert : chain) {
                certs.add((X509Certificate) cert);
            }
        }

        return certs;
    }
}
