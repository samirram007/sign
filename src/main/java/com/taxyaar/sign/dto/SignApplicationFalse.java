package com.taxyaar.sign.dto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SignApplicationFalse {
	private static final Logger LOGGER = LoggerFactory.getLogger(SignApplicationFalse.class);

	public static void main(String[] args) {
		SpringApplication.run(SignApplicationFalse.class, args);
	}

	// @PostConstruct
	// public void init() {
	// Security.addProvider(new BouncyCastleProvider());
	// }

	public static byte[] generateSign(String data, String eriId) throws Exception {
		System.out.println("Entering generateSign at " +
				System.currentTimeMillis());
		String pfxCred = "";
		String pfxType = "";
		String alias = "";
		Certificate[] certChain = null;
		List<Certificate> certList = null;

		JcaCertStore certStore = null;
		PrivateKey privKey = null;
		X509Certificate certificate = null;
		try {
			Security.addProvider((Provider) new BouncyCastleProvider());
			String certFilePath = "src/main/resources/cert/eri.pfx";
			pfxType = "PKCS12";
			pfxCred = "kushal94";
			alias = "agencykey";
			KeyStore keyStore = KeyStore.getInstance(pfxType, "BC");
			keyStore.load(new FileInputStream(new File(certFilePath)),
					pfxCred.toCharArray());
			certChain = keyStore.getCertificateChain(alias);
			certList = new ArrayList<>();
			for (int i = 0; i < certChain.length; i++)
				certList.add(certChain[i]);
			certStore = new JcaCertStore(certList);
			KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias,
					new KeyStore.PasswordProtection(pfxCred.toCharArray()));
			privKey = entry.getPrivateKey();
			certificate = (X509Certificate) keyStore.getCertificate(alias);
			X509CertificateHolder certificateHolder = new X509CertificateHolder(certificate.getEncoded());
			CMSSignedDataGenerator cmsSignedDataGenerator = new CMSSignedDataGenerator();
			ContentSigner sha1Signer = (new JcaContentSignerBuilder("SHA256withRSA")).setProvider("BC").build(privKey);
			cmsSignedDataGenerator
					.addSignerInfoGenerator(
							(new JcaSignerInfoGeneratorBuilder((new JcaDigestCalculatorProviderBuilder())
									.setProvider("BC").build()))
									.build(sha1Signer, certificateHolder));
			cmsSignedDataGenerator.addCertificates((Store) certStore);

			CMSProcessableByteArray cMSProcessableByteArray = new CMSProcessableByteArray(data.getBytes());
			CMSSignedData sigData = cmsSignedDataGenerator.generate((CMSTypedData) cMSProcessableByteArray, false);
			return sigData.getEncoded();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			LOGGER.info("Exit generateSign at " + System.currentTimeMillis());
		}
	}

	public static Boolean verifySignedData(final String signedData, final String dataToSign, String userId)
			throws CMSException, java.security.cert.CertificateExpiredException,
			java.security.cert.CertificateNotYetValidException {
		Boolean result = null;
		try {
			String filePath = "src/main/resources/cert/eri.pfx";
			if (filePath.isEmpty()) {
				result = false;
			} else {
				final X509Certificate x509Certificate = getX509Certificate(filePath);
				if (x509Certificate != null) {
					x509Certificate.checkValidity();
				}
				result = verifySignedData(signedData, dataToSign, x509Certificate);
			}

			LOGGER.info("data signature verified");
			return result;
		} catch (IllegalArgumentException e) {
			LOGGER.error("IllegalArgumentException occured in getArguments method {}", e);
			return false;
		} catch (final Exception e) {
			LOGGER.error("Exception in signature verification", e);
			return false;
		}
	}

	private static boolean verifySignedData(final String sign, final String data, final X509Certificate x509Certificate)
			throws CMSException, OperatorCreationException {

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		final CMSProcessableByteArray cmsProcessableByteArray = new CMSProcessableByteArray(
				Base64.decode(data.getBytes()));
		final CMSSignedData cms = new CMSSignedData(cmsProcessableByteArray, Base64.decode(sign.getBytes()));
		final SignerInformationStore signers = cms.getSignerInfos();
		final Collection<SignerInformation> c = signers.getSigners();

		for (final SignerInformation signer : c) {
			final boolean verify = signer
					.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(x509Certificate));
			return verify;
		}

		return false;
	}

	private static X509Certificate getX509Certificate(final String filePath) throws IOException {
		FileInputStream fis = null;
		try {
			final File file = new File(filePath);
			fis = new FileInputStream(file);
			KeyStore ks = KeyStore.getInstance("PKCS12");
			String pwd = "kushal94"; // replace
			LOGGER.debug("Keystore password fetched");

			ks.load(fis, pwd.toCharArray());
			Certificate cert = ks.getCertificate("agencykey");
			return (X509Certificate) cert;
		} catch (IllegalArgumentException e) {
			LOGGER.error("IllegalArgumentException occured in getArguments method {}", e);
		} catch (final Throwable th) {
			LOGGER.error("Throwable {} ", th);
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		return null;
	}

	public String getEncryptedPlainText(String plainText, SecretKey key) throws Exception {

		Cipher cipher = Cipher.getInstance("AES");
		byte[] plainTextByte = plainText.getBytes();
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedByte = cipher.doFinal(plainTextByte);
		java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
		return encoder.encodeToString(encryptedByte);
	}

	public String getDecryptedPlainText(String encryptedString, SecretKey key) throws Exception {

		Cipher cipher = Cipher.getInstance("AES");
		java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
		byte[] encryptedTextByte = decoder.decode(encryptedString);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
		return new String(decryptedByte);
	}
}
