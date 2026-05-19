package com.osamah.games.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Test
    @SuppressWarnings("unchecked")
    void sendPasswordResetEmail_ShouldSendJsonPayloadToBrevoCorrectly() {
        // Arrange
        String targetEmail = "osamah@test.com";
        String otpCode = "123456";
        String senderEmail = "springemailservice2534@gmail.com";

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        EmailService emailService = new EmailService("mock-key", senderEmail) {
            {
                try {
                    java.lang.reflect.Field field = EmailService.class.getDeclaredField("restClient");
                    field.setAccessible(true);
                    field.set(this, restClient);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        emailService.sendPasswordResetEmail(targetEmail, otpCode);

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestBodySpec).body(payloadCaptor.capture());

        Map<String, Object> capturedPayload = payloadCaptor.getValue();

        Map<String, String> sender = (Map<String, String>) capturedPayload.get("sender");
        assertThat(sender).containsEntry("name", "Games API");
        assertThat(sender).containsEntry("email", senderEmail);

        List<Map<String, String>> toList = (List<Map<String, String>>) capturedPayload.get("to");
        assertThat(toList).hasSize(1);
        assertThat(toList.get(0)).containsEntry("email", targetEmail);

        assertThat(capturedPayload).containsEntry("subject", "Password Reset OTP - Games API");
        String htmlContent = (String) capturedPayload.get("htmlContent");
        assertThat(htmlContent).contains(otpCode);

        verify(responseSpec, times(1)).toBodilessEntity();
    }

    @Test
    void sendPasswordResetEmail_ShouldThrowRuntimeException_WhenRestClientFails() {

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Connection timeout"));

        EmailService emailService = new EmailService("mock-key", "test@test.com") {
            {
                try {
                    java.lang.reflect.Field field = EmailService.class.getDeclaredField("restClient");
                    field.setAccessible(true);
                    field.set(this, restClient);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        assertThatThrownBy(() -> emailService.sendPasswordResetEmail("test@test.com", "111111")).isInstanceOf(
                        RuntimeException.class)
                .hasMessageContaining("Email delivery failed due to upstream HTTP exception");
    }
}