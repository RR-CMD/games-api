package com.osamah.games.auth;

import com.osamah.games.auth.dto.*;
import com.osamah.games.exception.BadRequestException;
import com.osamah.games.exception.DuplicateResourceException;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.security.JwtService;
import com.osamah.games.user.User;
import com.osamah.games.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private OtpRepository otpRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    //REGISTER TESTS
    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        UserRegisterRequest req = new UserRegisterRequest("osamah", "taken@mail.com", "Pass123!");
        when(userRepository.existsByEmail("taken@mail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        UserRegisterRequest req = new UserRegisterRequest("takenUser", "new@mail.com", "Pass123!");
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(userRepository.existsByUsername("takenUser")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldSaveUserAndReturnToken_WhenValid() {
        UserRegisterRequest req = new UserRegisterRequest("osamah", "new@mail.com", "Pass123!");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));
        when(jwtService.generateToken(any())).thenReturn("validToken");

        AuthResponse response = authService.register(req);

        assertThat(response.token()).isEqualTo("validToken");
        verify(userRepository).save(any(User.class));
    }

    //LOGIN TESTS
    @Test
    void login_ShouldThrowException_WhenUserNotFoundInDb() {
        UserLoginRequest req = new UserLoginRequest("false@mail.com", "pass");

        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.login(req));


        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {
        UserLoginRequest req = new UserLoginRequest("osamah@mail.com", "WrongPassword123!");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(req));

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        UserLoginRequest req = new UserLoginRequest("osamah@mail.com", "pass");
        User user = new User();
        user.setEmail("osamah@mail.com");

        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));
        when(jwtService.generateToken(any())).thenReturn("validToken");

        AuthResponse response = authService.login(req);

        assertThat(response.token()).isEqualTo("validToken");
        verify(authenticationManager).authenticate(any());
    }

    //FORGOT PASSWORD TESTS
    @Test
    void forgotPassword_ShouldThrowException_WhenEmailNotFound() {
        ForgotPasswordRequest req = new ForgotPasswordRequest("nobody@mail.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.forgotPassword(req));


        verify(otpRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void forgotPassword_ShouldDeleteOldOtpsAndSendEmail_WhenUserExists() {
        ForgotPasswordRequest req = new ForgotPasswordRequest("user@mail.com");
        User user = new User();
        user.setEmail("user@mail.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        authService.forgotPassword(req);

        verify(otpRepository).deleteByEmail("user@mail.com");
        verify(otpRepository).save(any(Otp.class));
        verify(emailService).sendPasswordResetEmail(eq("user@mail.com"), anyString());
    }

    @Test
    void forgotPassword_ShouldSaveOtpWithFutureExpiryAndCorrectEmail() {
        ForgotPasswordRequest req = new ForgotPasswordRequest("user@mail.com");
        User user = new User();
        user.setEmail("user@mail.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        authService.forgotPassword(req);

        verify(otpRepository).save(argThat(otp -> otp.getEmail()
                .equals("user@mail.com") && otp.getExpiryDate()
                .isAfter(Instant.now()) && otp.getCode()
                .length() >= 6));
    }

    //RESET PASSWORD TESTS
    @Test
    void resetPassword_ShouldThrowException_WhenOtpIsWrong() {
        ResetPasswordRequest req = new ResetPasswordRequest("test@mail.com", "000000", "NewPass123!");
        when(otpRepository.findByEmailAndCode("test@mail.com", "000000")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.resetPassword(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_ShouldThrowException_WhenOtpIsExpired() {
        ResetPasswordRequest req = new ResetPasswordRequest("test@mail.com", "123456", "NewPass123!");
        Otp expiredOtp = Otp.builder()
                .expiryDate(Instant.now()
                        .minus(1, ChronoUnit.MINUTES))
                .build();

        when(otpRepository.findByEmailAndCode(anyString(), anyString())).thenReturn(Optional.of(expiredOtp));

        assertThrows(BadRequestException.class, () -> authService.resetPassword(req));
        verify(otpRepository).delete(expiredOtp);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_ShouldUpdatePassword_WhenValid() {
        ResetPasswordRequest req = new ResetPasswordRequest("test@mail.com", "123456", "NewPass123!");
        Otp validOtp = Otp.builder()
                .expiryDate(Instant.now()
                        .plus(10, ChronoUnit.MINUTES))
                .build();
        User mockUser = new User();

        when(otpRepository.findByEmailAndCode(anyString(), anyString())).thenReturn(Optional.of(validOtp));
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("NewPass123!")).thenReturn("newHashedPass");

        authService.resetPassword(req);

        assertThat(mockUser.getPassword()).isEqualTo("newHashedPass");
        verify(userRepository).save(mockUser);
        verify(otpRepository).delete(validOtp);
    }

    //CLEAN-UP EXPIRED OTPs TEST
    @Test
    void cleanupExpiredOtps_ShouldCallRepositoryDelete() {
        authService.cleanupExpiredOtps();

        verify(otpRepository).deleteByExpiryDateBefore(any(Instant.class));
    }
}