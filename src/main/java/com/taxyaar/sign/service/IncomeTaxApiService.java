package com.taxyaar.sign.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxyaar.sign.crypto.CryptoUtil;
import com.taxyaar.sign.dto.request.LoginRequestDto;
import com.taxyaar.sign.dto.request.SignedDataRequestDto;
import com.taxyaar.sign.dto.response.SignedDataResponseDto;

@Service
public class IncomeTaxApiService {

    private static final System.Logger logger =
            System.getLogger(IncomeTaxApiService.class.getName());

    @Autowired
    private RestTemplate restTemplate;

    private final SignService signService;
    private final CryptoUtil cryptoUtil;

    public IncomeTaxApiService(SignService signService, CryptoUtil cryptoUtil) {
        this.signService = signService;
        this.cryptoUtil = cryptoUtil;
    }

    public String login() throws Exception {
        try {

            SignedDataRequestDto signedDataReq = new SignedDataRequestDto();
            signedDataReq.setDataToSign(preparePayload());
            System.out.println("ERIUSER ID: "+signedDataReq.getEriUserId());
            signedDataReq.setEriUserId("ERIP014181");

            SignedDataResponseDto signedRes =
                    signService.generate(signedDataReq);

            LoginRequestDto request = new LoginRequestDto();
            request.setSign(signedRes.getSign());
            request.setData(signedRes.getData());
            System.out.println("ERI USer ID: "+signedRes.getEriUserId());
            request.setEriUserId(signedRes.getEriUserId());

            logger.log(System.Logger.Level.INFO,
                    "Sending ERI login request for user: " +
                            request.getEriUserId());


            debugLoginRequest(request);

            return loginApiCall(request);

        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR,
                    "Login failed", ex);
            throw new RuntimeException("Login failed", ex);
        }
    }

    private String preparePayload() throws Exception {

        String serviceName = "EriLoginService";
        String entity = "ERIP014181";
        String plainText = "Oracle@123";
        String key = "fkR5WKAStrZ/IXqyPxjaKw==";

        String pass = cryptoUtil.encryptForEri(plainText, key);
        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        String timeStamp =
                OffsetDateTime.now(ZoneId.of("Asia/Kolkata")).format(fmt);

        System.out.println("Encrypted pass length: " + pass.length());
        System.out.println(pass);


        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("serviceName", serviceName);
        payload.put("entity", entity);
        payload.put("pass", pass);
        payload.put("timeStamp", timeStamp);


        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(payload);
    }

    private String loginApiCall(LoginRequestDto request)
            throws JsonProcessingException {

        String url = "https://uatocpservices.incometax.gov.in/iec-uat/uat/eriapi/login";

        System.out.println("Request User ID: "+ request.getEriUserId());

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("clientId", "d679465a50168a9ee14e0035a0199e6f");
            headers.set("clientSecret", "978cd4c239602bf2e7c0b441b42c503d");
            headers.set("accessMode", "API");

            String decoded = new String(
                    Base64.getDecoder().decode(request.getData()),
                    StandardCharsets.UTF_8
            );

            System.out.println("Decoded DATA:");
            System.out.println(decoded);

            HttpEntity<LoginRequestDto> entity =
                    new HttpEntity<>(request, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            logger.log(System.Logger.Level.INFO,
                    "ERI login response: " +
                            response.getStatusCode());

            return response.getBody();

        } catch (HttpStatusCodeException ex) {

            logger.log(System.Logger.Level.ERROR,
                    "HTTP error: " + ex.getStatusCode());

            logger.log(System.Logger.Level.ERROR,
                    "ERI response: " +
                            ex.getResponseBodyAsString());

            throw ex;

        } catch (ResourceAccessException ex) {

            logger.log(System.Logger.Level.ERROR,
                    "Connection error", ex);

            throw ex;

        } catch (RestClientException ex) {

            logger.log(System.Logger.Level.ERROR,
                    "RestTemplate error", ex);

            throw ex;

        } catch (Exception ex) {

            logger.log(System.Logger.Level.ERROR,
                    "Unexpected error", ex);

            throw new RuntimeException(
                    "Unknown login error", ex);
        }
    }

    private void debugLoginRequest(LoginRequestDto req) throws Exception {

        String rawJson = new String(
                Base64.getDecoder().decode(req.getData()),
                StandardCharsets.UTF_8
        );

        System.out.println("\n===== ERI DEBUG =====");
        System.out.println("RAW JSON (signed):");
        System.out.println(rawJson);

        System.out.println("\nBASE64 DATA:");
        System.out.println(req.getData());

        System.out.println("\nSIGNATURE:");
        System.out.println(req.getSign());

        ObjectMapper mapper = new ObjectMapper();
        System.out.println("\nFINAL REQUEST JSON:");
        System.out.println(mapper.writeValueAsString(req));

        System.out.println("=====================\n");
    }
}


