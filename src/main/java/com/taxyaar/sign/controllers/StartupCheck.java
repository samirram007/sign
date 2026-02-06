package com.taxyaar.sign.controllers;

import org.springframework.stereotype.Component;

import com.taxyaar.sign.crypto.CryptoUtil;

@Component
public class StartupCheck {
    public StartupCheck(CryptoUtil cryptoUtil) {
        System.out.println("CryptoUtil bean loaded");
    }
}