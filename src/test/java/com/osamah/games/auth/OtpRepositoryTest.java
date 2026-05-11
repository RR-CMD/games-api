package com.osamah.games.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OtpRepositoryTest {

    @Autowired
    private OtpRepository otpRepository;

    @BeforeEach
    void setUp() {
        otpRepository.save(Otp.builder()
                .email("expired@mail.com")
                .code("111111")
                .expiryDate(Instant.now()
                        .minus(5, ChronoUnit.MINUTES))
                .build());

        otpRepository.save(Otp.builder()
                .email("valid@mail.com")
                .code("222222")
                .expiryDate(Instant.now()
                        .plus(15, ChronoUnit.MINUTES))
                .build());
    }

    @Test
    void deleteByExpiryDateBefore_ShouldOnlyDeleteExpiredOtps() {

        otpRepository.deleteByExpiryDateBefore(Instant.now());

        assertThat(otpRepository.findAll()).hasSize(1);
        assertThat(otpRepository.findAll()
                .getFirst()
                .getEmail()).isEqualTo("valid@mail.com");
    }

    @Test
    void deleteByEmail_ShouldDeleteSpecificOtp() {
        otpRepository.deleteByEmail("valid@mail.com");

        assertThat(otpRepository.findAll()).hasSize(1);
        assertThat(otpRepository.findAll()
                .getFirst()
                .getEmail()).isEqualTo("expired@mail.com");
    }
}