package com.example.webhook_solver.service;

import com.example.webhook_solver.model.FinalQueryRequest;
import com.example.webhook_solver.model.GenerateWebhookRequest;
import com.example.webhook_solver.model.GenerateWebhookResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookService {
    private final Logger logger = LoggerFactory.getLogger(WebhookService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GENERATE_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    // public GenerateWebhookResponse generateWebhook(String name, String regNo,
    // String email) {
    // GenerateWebhookRequest request = new GenerateWebhookRequest(name, regNo,
    // email);
    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_JSON);
    // HttpEntity<GenerateWebhookRequest> entity = new HttpEntity<>(request,
    // headers);

    // logger.info("Calling generate webhook API...");
    // ResponseEntity<GenerateWebhookResponse> response =
    // restTemplate.exchange(GENERATE_URL, HttpMethod.POST, entity,
    // GenerateWebhookResponse.class);

    // if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() ==
    // HttpStatus.CREATED) {
    // logger.info("Received webhook response");
    // GenerateWebhookResponse body = response.getBody();
    // logger.info("Webhook URL received = {}", body.getData().getWebhookUrl());
    // logger.info("Access Token = {}", body.getData().getAccessToken());
    // return body;
    // // return response.getBody();
    // } else {
    // throw new IllegalStateException("Failed to generate webhook: " +
    // response.getStatusCode());
    // }
    // }

    public GenerateWebhookResponse generateWebhook(String name, String regNo, String email) {
        GenerateWebhookRequest request = new GenerateWebhookRequest(name, regNo, email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GenerateWebhookRequest> entity = new HttpEntity<>(request, headers);

        logger.info("Calling generate webhook API...");

        // STEP 1: Get RAW JSON as String first
        ResponseEntity<String> rawResponse = restTemplate.exchange(
                GENERATE_URL,
                HttpMethod.POST,
                entity,
                String.class);

        logger.error("RAW JSON RESPONSE = {}", rawResponse.getBody()); // <-- PRINT THIS

        // STEP 2: Now attempt to map to GenerateWebhookResponse
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(rawResponse.getBody(), GenerateWebhookResponse.class);
        } catch (Exception e) {
            logger.error("❌ Failed to parse JSON into GenerateWebhookResponse");
            logger.error("Error = {}", e.getMessage());
            return null;
        }
    }

    // public void submitFinalQuery(String webhookUrl, String accessToken, String
    // finalQuery) {
    // HttpHeaders headers = new HttpHeaders();
    // // Set authorization header. Use bearer token.
    // headers.setBearerAuth(accessToken);
    // headers.setContentType(MediaType.APPLICATION_JSON);

    // FinalQueryRequest req = new FinalQueryRequest(finalQuery);
    // HttpEntity<FinalQueryRequest> entity = new HttpEntity<>(req, headers);

    // logger.info("Submitting final query to webhook: {}", webhookUrl);
    // ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl,
    // entity, String.class);

    // if (response.getStatusCode().is2xxSuccessful()) {
    // logger.info("Final query submitted successfully. Response: {}",
    // response.getBody());
    // } else {
    // logger.error("Failed to submit final query. Status: {}, Body: {}",
    // response.getStatusCode(),
    // response.getBody());
    // throw new IllegalStateException("Failed to submit final query: " +
    // response.getStatusCode());
    // }
    // }

    public String submitFinalQuery(String webhookUrl, String token, String finalSql) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token); // use explicit header

        Map<String, String> body = new HashMap<>();
        body.put("query", finalSql);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println(
                    "Error submitting final webhook query: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

}
