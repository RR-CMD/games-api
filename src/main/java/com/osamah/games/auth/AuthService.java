package com.osamah.games.auth;

import com.osamah.games.auth.dto.*;
import com.osamah.games.exception.BadRequestException;
import com.osamah.games.exception.DuplicateResourceException;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.security.JwtService;
import com.osamah.games.user.User;
import com.osamah.games.user.UserRepository;
import com.osamah.games.user.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already taken.");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username is already taken.");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponse(user.getId(), jwtToken, "Registration successful");
    }


    public AuthResponse login(UserLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password()));

        User user = userRepository.findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponse(user.getId(), jwtToken, "Login successful");
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("No user " + "with this email " + "found"));

        otpRepository.deleteByEmail(user.getEmail());

        int otpNumber = 100000 + SECURE_RANDOM.nextInt(900000);
        String generatedOtp = String.valueOf(otpNumber);

        Otp otp = Otp.builder()
                .email(user.getEmail())
                .code(generatedOtp)
                .expiryDate(Instant.now()
                        .plus(15, ChronoUnit.MINUTES))
                .build();

        otpRepository.save(otp);
        emailService.sendPasswordResetEmail(user.getEmail(), generatedOtp);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Otp otp = otpRepository.findByEmailAndCode(request.email(), request.otp())
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        if (otp.getExpiryDate()
                .isBefore(Instant.now())) {
            otpRepository.delete(otp);
            throw new BadRequestException("OTP has expired");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User " + "not found"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        otpRepository.delete(otp);

    }

    /*
    NOTE: The scheduling below is heavy and should only be used for small-scale projects and testing.
         For real-world apps, we should instead use something like @Scheduled(cron = "0 0 3 * * TUE") to
         clean expired OTPs every Tuesday at 3 AM (server-time).
    */
    @Scheduled(fixedRate = 120000)
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiryDateBefore(Instant.now());
    }
}