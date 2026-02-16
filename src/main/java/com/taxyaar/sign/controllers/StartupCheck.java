package com.taxyaar.sign.controllers;

import org.springframework.stereotype.Component;

import com.taxyaar.sign.crypto.CryptoUtil;

@Component
public class StartupCheck {
    public StartupCheck(CryptoUtil cryptoUtil) {
        // This constructor will be called when the application starts, and the
        // CryptoUtil bean will be injected

        System.out.println("CryptoUtil bean loaded");
    }
}