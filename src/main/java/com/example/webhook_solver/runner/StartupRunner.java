package com.example.webhook_solver.runner;

import com.example.webhook_solver.model.GenerateWebhookResponse;
import com.example.webhook_solver.service.QuestionSolverService;
import com.example.webhook_solver.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(StartupRunner.class);
    private final WebhookService webhookService;
    private final QuestionSolverService solverService;

    private final String NAME = "Mohamed Aariz";
    private final String REGNO = "22BCE3701";
    private final String EMAIL = "aariz4ever@gmail.com";

    public StartupRunner(WebhookService webhookService, QuestionSolverService solverService) {
        this.webhookService = webhookService;
        this.solverService = solverService;
    }

    @Override
    public void run(String... args) throws Exception {

        logger.info("Starting automatic webhook flow...");

        // 1. Generate webhook
        GenerateWebhookResponse resp = webhookService.generateWebhook(NAME, REGNO, EMAIL);

        if (resp == null || resp.getWebhook() == null || resp.getAccessToken() == null) {
            logger.error("Invalid response from generateWebhook: {}", resp);
            throw new IllegalStateException("generateWebhook failed or returned incomplete data");
        }

        String webhookUrl = resp.getWebhook();
        String accessToken = resp.getAccessToken();

        logger.info("Webhook URL: {}", webhookUrl);
        logger.info("Access Token received (truncated): {}",
                accessToken.length() > 10 ? accessToken.substring(0, 10) + "..." : accessToken);

        // 2. Prepare final SQL
        String finalSql = solverService.getFinalQueryForRegNo(REGNO);
        logger.info("Prepared Final SQL (length={} chars)", finalSql.length());

        // 3. Submit final SQL
        webhookService.submitFinalQuery(webhookUrl, accessToken, finalSql);

        logger.info("Flow completed.");
    }
}
