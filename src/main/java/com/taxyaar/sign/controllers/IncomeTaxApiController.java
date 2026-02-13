package com.taxyaar.sign.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxyaar.sign.service.IncomeTaxApiService;

@RestController
@RequestMapping("/api")
public class IncomeTaxApiController {
    private final IncomeTaxApiService service;

    public IncomeTaxApiController(IncomeTaxApiService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public String Login() throws Exception {

        return service.login();
    }
}
