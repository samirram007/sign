package com.taxyaar.sign.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.taxyaar.sign.crypto.CryptoUtil;

@Configuration
public class CryptoConfig {

    @Bean
    public CryptoUtil cryptoUtil() {
        return new CryptoUtil();
    }
}
