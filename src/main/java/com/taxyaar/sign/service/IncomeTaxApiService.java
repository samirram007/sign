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
import com.taxyaar.sign.dto.request.LoginRequestDto;
import com.taxyaar.sign.dto.request.SignedDataRequestDto;
import com.taxyaar.sign.dto.response.SignedDataResponseDto;

@Service
public class IncomeTaxApiService {
    @Autowired
    private RestTemplate restTemplate;
    private final SignService signService;

    public IncomeTaxApiService(SignService signService) {
        this.signService = signService;
    }

    public String login() throws Exception {
        // This method will handle the login process to the income tax API
        // It will prepare the payload, generate the signature using the SignService,
        // and make the API call to get the access token
        // You can implement the logic to prepare the payload, generate the signature,
        // and make the API call as per the requirements of the income tax API
        // For demonstration, let's assume we have a method called PreparePayload that
        // prepares the payload for the login request
        try {

            SignedDataRequestDto signedDataReq = new SignedDataRequestDto();
            signedDataReq.setDataToSign(PreparePayload());
            signedDataReq.setEriUserId(signedDataReq.getEriUserId());

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

    public String PreparePayload() throws Exception {

        // Prepare the payload for the income tax API login request
        // This is a sample payload, you can modify it as per the API requirements
        // The payload typically includes serviceName, entity, pass (encrypted
        // password), and timeStamp
        // You can use the SignService to get the encrypted password if needed
        // For demonstration, let's assume we have the following values for the payload

        String serviceName = "EriLoginService";
        String entity = "ERIP014181";
        // String pass = "FHikN7R8KSTNoU9PBqm9OueFGJ9pIQuLLxz65UrnL+c=";
        String plainText = "Oracle@123";
        String key = "fkR5WKAStrZ/IXqyPxjaKw==";

        String pass = this.signService.getEncryptedPlainText(plainText, key);
        // System.out.println("Encrypted Password: " + pass);
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
        // You can use a library like Jackson's ObjectMapper to convert the Map to a
        // JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(payload);

        // 2️⃣ Convert JSON string to Base64
        // Encode the JSON string to Base64 using Java's Base64 encoder

        String base64Encoded = Base64.getEncoder().encodeToString(jsonString.getBytes(StandardCharsets.UTF_8));

        return base64Encoded;
    }

    private String LoginApiCall(LoginRequestDto requestBody) {

        // Prepare the headers for the API call, including clientId, clientSecret, and
        // accessMode
        // You can set these values as per the API requirements
        // For demonstration, let's assume we have the following values for the headers
        // clientId: d679465a50168a9ee14e0035a0199e6f
        // clientSecret: 978cd4c239602bf2e7c0b441b42c503d
        // accessMode: API
        // The API endpoint for login is
        // https://uatocpservices.incometax.gov.in/iec-uat/uat/eriapi/login
        // You can use RestTemplate to make the API call and get the response
        // The response will typically contain the access token or any relevant
        // information returned by the API
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
