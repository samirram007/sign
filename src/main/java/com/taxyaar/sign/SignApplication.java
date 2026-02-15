package com.taxyaar.sign;

import com.taxyaar.sign.config.TaxConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SignApplication {
//	 private static final Logger LOGGER =
//	 LoggerFactory.getLogger(SignApplication.class);

    @Autowired
    private TaxConfig taxConfig;

	public static void main(String[] args) {
		SpringApplication.run(SignApplication.class, args);
		// StartupCheck startupCheck = new StartupCheck(new
		// com.taxyaar.sign.crypto.CryptoUtil());

	}
    // @PostConstruct
	// public void init() {
	// Security.addProvider(new BouncyCastleProvider());
	// }

}
