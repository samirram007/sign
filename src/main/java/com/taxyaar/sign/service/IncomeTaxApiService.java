package com.taxyaar.sign.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.taxyaar.sign.config.TaxConfig;
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
import com.taxyaar.sign.dto.request.LoginRequestDto;
import com.taxyaar.sign.dto.request.SignedDataRequestDto;
import com.taxyaar.sign.dto.response.SignedDataResponseDto;

@Service
public class IncomeTaxApiService {

    private static final System.Logger logger = System.getLogger(IncomeTaxApiService.class.getName());


    UUID uuid = UUID.randomUUID();
    @Autowired
    private RestTemplate restTemplate;

    private final SignService signService;
    private final CryptoUtil cryptoUtil;
    @Autowired
    private TaxConfig taxConfig;

    public IncomeTaxApiService(SignService signService) {
        this.signService = signService;
    }

    public String login() throws Exception {

        // build JSON once
        String rawJson = preparePayload();

        // freeze bytes once
        byte[] rawBytes = rawJson.getBytes(StandardCharsets.UTF_8);

        // sign exact bytes
        byte[] signature = signService.sign(rawBytes);

        LoginRequestDto request = new LoginRequestDto();
        request.setData(Base64.getEncoder().encodeToString(rawBytes));
        request.setSign(Base64.getEncoder().encodeToString(signature));
        request.setEriUserId(taxConfig.getEriUserId());

        debugLoginRequest(request);

        return loginApiCall(request);
    }

//    public String login() throws Exception {
//        try {
//
//            SignedDataRequestDto signedDataReq = new SignedDataRequestDto();
//            signedDataReq.setDataToSign(preparePayload());
//            System.out.println("ERIUSER ID: "+taxConfig.getEriUserId());
//            signedDataReq.setEriUserId(taxConfig.getEriUserId());
//
//            SignedDataResponseDto signedRes =
//                    signService.generate(signedDataReq);
//
//            LoginRequestDto request = new LoginRequestDto();
//            request.setSign(signedRes.getSign());
//            request.setData(signedRes.getData());
//            request.setEriUserId(taxConfig.getEriUserId());
//
//            logger.log(System.Logger.Level.INFO,
//                    "Sending ERI login request for user: " +
//                            request.getEriUserId());
//
//
//            debugLoginRequest(request);
//
//            return loginApiCall(request);
//
//        } catch (Exception ex) {
//            logger.log(System.Logger.Level.ERROR,
//                    "Login failed", ex);
//            throw new RuntimeException("Login failed", ex);
//        }
//    }

    private String preparePayload() throws Exception {

        String pass = cryptoUtil.encryptForEri(taxConfig.getEriPlainText(), taxConfig.getEriPasswordKey());
        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        String timeStamp =
                OffsetDateTime.now(ZoneId.of("Asia/Kolkata")).format(fmt);

        System.out.println("Encrypted pass length: " + pass.length());
        System.out.println(pass);


        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("serviceName", taxConfig.getEriServiceName());
        payload.put("entity", taxConfig.getEriUserId());
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

//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("clientId", taxConfig.getEriClientId());
//            headers.set("clientSecret", taxConfig.getEriClientSecret());
//            headers.set("accessMode", taxConfig.getEriAccessMode());
//            headers.set("User-Agent",taxConfig.getEriUserAgent());
//            headers.set("X-Request-ID",uuid.toString());
//            headers.set("Host", "uatocpservices.incometax.gov.in");


            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            headers.set("clientId", taxConfig.getEriClientId());
            headers.set("clientSecret", taxConfig.getEriClientSecret());
            headers.set("accessMode", taxConfig.getEriAccessMode());

            headers.set("User-Agent", "TaxYaar-ERI-Client/1.0");
            headers.set("X-Request-ID", UUID.randomUUID().toString());

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


