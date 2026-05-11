package com.osamah.games.auth;

import com.osamah.games.auth.dto.*;
import com.osamah.games.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Public endpoints for registration, login, and password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "Register a new user (Logged-Out Only)", description = "Creates a new user account and " +
            "returns a JWT token")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "Login (Logged-Out Only)", description =
            "Authenticates a user via email or username and " + "returns a JWT token")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/forgot-password")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "Forgot Password (Logged-Out Only)", description =
            "Generates a 6-digit OTP and sends it " + "via" + " " + "email")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.success(null, "An OTP has been sent to the email entered.");
    }

    @PostMapping("/reset-password")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "Reset Password (Logged-Out Only)", description =
            "Verifies the OTP and updates the user's " + "password")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success(null, "Password has been reset successfully.");
    }
}