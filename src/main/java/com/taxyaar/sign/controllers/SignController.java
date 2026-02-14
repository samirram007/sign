package com.taxyaar.sign.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxyaar.sign.dto.request.EncryptedPlanText;
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
        // Calling the service method to generate the signature
        // and returning the response as a SignedDataResponseDto
        // Assuming the service method generate takes the SignedDataRequestDto as input
        // and returns a SignedDataResponseDto containing the signed data and related
        // information
        return (SignedDataResponseDto) service.generate(request);
    }

    @PostMapping("/verify")
    public boolean verify(@RequestBody VerifySignRequestDto req)
            throws Exception {
        // Calling the service method to verify the signature
        // and returning the result as a boolean
        // Assuming the service method verify takes the VerifySignRequestDto as input
        // and returns a boolean indicating the verification result
        return service.verify(req);
    }

    @PostMapping("/verify_cert")
    public boolean verifyCert(@RequestBody VerifySignRequestDto req)
            throws Exception {
        // Calling the service method to verify the signature using the certificate
        // and returning the result as a boolean
        // Assuming the service method verifyCert takes the VerifySignRequestDto as
        // input and returns a boolean indicating the verification result
        return service.verifyCert(req);
    }

    @PostMapping("/encrypted-plain-text")
    public String getEncryptedPlainText(@RequestBody EncryptedPlanText request)
            throws Exception {
        // Calling the service method to get the encrypted plain text
        // and returning the result as a String
        // Assuming the service method getEncryptedPlainText takes the plain text and
        // key from the request and returns the encrypted plain text
        return service.getEncryptedPlainText(request.getPlainText(), request.getKey());
    }


}
