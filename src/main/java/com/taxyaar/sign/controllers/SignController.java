package com.taxyaar.sign.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxyaar.sign.dto.request.SignedDataRequestDto;
import com.taxyaar.sign.dto.request.VerifySignRequestDto;
import com.taxyaar.sign.dto.response.SignedDataResponseDto;
import com.taxyaar.sign.service.SignService;

@RestController
@RequestMapping("/api")
public class SignController {

    private final SignService service;

    public SignController(SignService service) {
        this.service = service;
    }

    @PostMapping("/sign")
    public SignedDataResponseDto sign(@RequestBody SignedDataRequestDto request)
            throws Exception {
        return (SignedDataResponseDto) service.generate(request);
    }

    @PostMapping("/verify")
    public boolean verify(@RequestBody VerifySignRequestDto req)
            throws Exception {
        return service.verify(req);
    }
}
