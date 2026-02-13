package com.taxyaar.sign.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxyaar.sign.crypto.CryptoUtil;
import com.taxyaar.sign.dto.request.LoginRequestDto;
import com.taxyaar.sign.dto.request.SignedDataRequestDto;
import com.taxyaar.sign.dto.response.SignedDataResponseDto;

@Service
public class IncomeTaxApiService {
    @Autowired
    private RestTemplate restTemplate;
    private SignService signService;
    private CryptoUtil cryptoUtil;

    public IncomeTaxApiService(SignService signService, CryptoUtil cryptoUtil) {
        this.signService = signService;
        this.cryptoUtil = cryptoUtil;
    }

    public String login() throws Exception {
        try {

            SignedDataRequestDto signedDataReq = new SignedDataRequestDto();
            signedDataReq.setDataToSign(PreparePayload());
            signedDataReq.setEriUserId("ERIP014181");

            LoginRequestDto requestBody = new LoginRequestDto();
            SignedDataResponseDto signedResBody = this.signService.generate(signedDataReq);

            requestBody.setSign(signedResBody.getSign());
            requestBody.setData(signedResBody.getData());
            requestBody.setEriUserId(signedResBody.getEriUserId());

            return this.LoginApiCall(requestBody);
        } catch (Exception ex) {
            System.getLogger(IncomeTaxApiService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;

    }

    private String PreparePayload() throws Exception {
        String serviceName = "EriLoginService";
        String entity = "ERIP014181";
        // String pass = "FHikN7R8KSTNoU9PBqm9OueFGJ9pIQuLLxz65UrnL+c=";
        String plainText = "Oracle@123";
        String key = "fkR5WKAStrZ/IXqyPxjaKw==";
        String pass = this.cryptoUtil.encryptForEri(plainText, key);
        System.out.println("Encrypted Password: " + pass);
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("serviceName", serviceName);
        payload.put("entity", entity);
        payload.put("pass", pass);
        payload.put("timeStamp", timeStamp);

        return this.convertToBase64(payload);

    }

    private String convertToBase64(Map<String, Object> payload) throws Exception {

        // 1️⃣ Convert Map to JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(payload);

        // 2️⃣ Convert JSON string to Base64
        String base64Encoded = Base64.getEncoder().encodeToString(jsonString.getBytes(StandardCharsets.UTF_8));

        return base64Encoded;
    }

    private String LoginApiCall(LoginRequestDto requestBody) {
        String url = "https://uatocpservices.incometax.gov.in/iec-uat/uat/eriapi/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("clientId", "d679465a50168a9ee14e0035a0199e6f");
        headers.set("clientSecret", "978cd4c239602bf2e7c0b441b42c503d");
        headers.set("accessMode", "API");

        HttpEntity<LoginRequestDto> entity = new HttpEntity<>(requestBody, headers);
        System.Logger logger = System.getLogger(IncomeTaxApiService.class.getName());
        logger.log(System.Logger.Level.INFO, "Request Body: " + requestBody);
        logger.log(System.Logger.Level.INFO, "Headers: " + headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getBody();
    }

}
