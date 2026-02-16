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

    @PostMapping("/loginnn")
    public String Login() throws Exception {
        // Calling the service method to perform login and get the access token
        // and returning the result as a String
        // Assuming the service method login takes the necessary input and returns a
        // String containing the access token
        return service.login();
    }

    @PostMapping("prepare-data")
    public String prepareData() throws Exception {
        // Calling the service method to prepare the payload for the income tax API
        // and returning the result as a String
        // Assuming the service method PreparePayload takes the necessary input and
        // returns a String containing the prepared payload
        return service.PreparePayload();
    }
}
