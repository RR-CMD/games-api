package com.osamah.games.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendPasswordResetEmail_ShouldFormatAndSendEmailCorrectly() {

        String targetEmail = "osamah@test.com";
        String otpCode = "123456";

        emailService.sendPasswordResetEmail(targetEmail, otpCode);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();


        assertThat(capturedMessage.getTo()).containsExactly(targetEmail);
        assertThat(capturedMessage.getSubject()).isEqualTo("Password Reset OTP - Games API");
        assertThat(capturedMessage.getText()).contains(otpCode);
        assertThat(capturedMessage.getText()).contains("expire in 15 minutes");
    }
}