package com.taxyaar.sign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SignApplication {
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(SignApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SignApplication.class, args);
	}

	// @PostConstruct
	// public void init() {
	// Security.addProvider(new BouncyCastleProvider());
	// }

}
