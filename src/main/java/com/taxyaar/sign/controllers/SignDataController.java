package com.taxyaar.sign.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api", produces = "application/json")
public class SignDataController {

    @GetMapping("/sign-data")
    public String generateSign() {
        return "Sign Data Response";
    }

}
