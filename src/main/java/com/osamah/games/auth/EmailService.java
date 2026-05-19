package com.osamah.games.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final RestClient restClient;
    private final String senderEmail;

    public EmailService(
            @Value("${brevo.api.key}") String apiKey,
            @Value("${brevo.sender.email}") String senderEmail) {

        this.senderEmail = senderEmail;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
    }

    public void sendPasswordResetEmail(String toEmail, String otpCode) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "sender", Map.of("name", "Games API", "email", this.senderEmail),
                    "to", List.of(Map.of("email", toEmail)),
                    "subject", "Password Reset OTP - Games API",
                    "htmlContent", "<p>Your OTP for resetting your password is: <strong>" + otpCode + "</strong></p>" +
                            "<p>This code will expire in 15 minutes.</p>" +
                            "<p style='color: gray; font-size: 12px;'>If you did not request this, please ignore this email.</p>"
            );

            restClient.post()
                    .uri("/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();


        } catch (Exception e) {
            throw new RuntimeException("Email delivery failed due to upstream HTTP exception", e);
        }
    }
}