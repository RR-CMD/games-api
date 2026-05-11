package com.osamah.games.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset OTP - Games API");
        message.setText(
                "Your OTP for resetting your password is: " + otpCode + "\n\nThis code will expire in 15 " + "minutes"
                        + ".\n" + "If you did not request this, please ignore this email.");

        mailSender.send(message);
    }
}