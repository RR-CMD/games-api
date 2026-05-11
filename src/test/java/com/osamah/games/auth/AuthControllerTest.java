package com.osamah.games.auth;

import com.osamah.games.auth.dto.*;
import com.osamah.games.exception.BadRequestException;
import com.osamah.games.exception.DuplicateResourceException;
import com.osamah.games.exception.ResourceNotFoundException;
import com.osamah.games.security.CustomAuthenticationEntryPoint;
import com.osamah.games.security.JwtFilter;
import com.osamah.games.security.JwtService;
import com.osamah.games.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtFilter.class, CustomAuthenticationEntryPoint.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    // REGISTER ENDPOINT TESTS
    @Test
    @WithAnonymousUser
    void register_ShouldReturn200_WhenValidAndAnonymous() throws Exception {
        UserRegisterRequest req = new UserRegisterRequest("osamah", "test@mail.com", "Password123!");
        AuthResponse mockResponse = new AuthResponse(1L, "mocktoken", "Registration successful");

        when(authService.register(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mocktoken"));
    }

    @Test
    @WithMockUser
    void register_ShouldReturn403_WhenUserIsAlreadyLoggedIn() throws Exception {
        UserRegisterRequest req = new UserRegisterRequest("osamah", "test@mail.com", "Password123!");

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void register_ShouldReturn400_WhenPasswordIsWeak() throws Exception {
        UserRegisterRequest req = new UserRegisterRequest("osamah", "test@mail.com", "weakpass");

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void register_ShouldReturn409_WhenUserAlreadyExists() throws Exception {
        UserRegisterRequest req = new UserRegisterRequest("osamah", "taken@mail.com", "Password123!");

        when(authService.register(any())).thenThrow(new DuplicateResourceException("Email is already taken."));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email is already taken."));
    }

    // LOGIN ENDPOINT TESTS
    @Test
    @WithAnonymousUser
    void login_ShouldReturn200_WhenValidAndAnonymous() throws Exception {
        UserLoginRequest req = new UserLoginRequest("osamah@mail.com", "Password123!");
        AuthResponse mockResponse = new AuthResponse(1L, "mocktoken", "Login successful");

        when(authService.login(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("mocktoken"));
    }

    @Test
    @WithMockUser
    void login_ShouldReturn403_WhenUserIsAlreadyLoggedIn() throws Exception {
        UserLoginRequest req = new UserLoginRequest("osamah@mail.com", "Password123!");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void login_ShouldReturn400_WhenFieldsAreBlank() throws Exception {
        UserLoginRequest req = new UserLoginRequest("", "");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void login_ShouldReturn401_WhenCredentialsAreInvalid() throws Exception {
        UserLoginRequest req = new UserLoginRequest("osamah@mail.com", "WrongPass123!");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Invalid email or password."));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    // FORGOT PASSWORD ENDPOINT TESTS
    @Test
    @WithAnonymousUser
    void forgotPassword_ShouldReturn200_WhenValidAndAnonymous() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest("test@mail.com");

        mockMvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("An OTP has been sent to the email entered."));
    }

    @Test
    @WithMockUser
    void forgotPassword_ShouldReturn403_WhenUserIsAlreadyLoggedIn() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest("test@mail.com");

        mockMvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void forgotPassword_ShouldReturn400_WhenEmailIsInvalid() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest("not-an-email");

        mockMvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // RESET PASSWORD ENDPOINT TESTS
    @Test
    @WithAnonymousUser
    void resetPassword_ShouldReturn200_WhenValidAndAnonymous() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest("test@mail.com", "123456", "NewPass123!");

        mockMvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully."));
    }

    @Test
    @WithMockUser
    void resetPassword_ShouldReturn403_WhenUserIsAlreadyLoggedIn() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest("test@mail.com", "123456", "NewPass123!");

        mockMvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void resetPassword_ShouldReturn400_WhenOtpIsMissing() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest("test@mail.com", "", "NewPass123!");

        mockMvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithAnonymousUser
    void resetPassword_ShouldReturn400_WhenOtpIsInvalidOrExpired() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest("test@mail.com", "000000", "NewPass123!");

        doThrow(new BadRequestException("Invalid or expired OTP")).when(authService)
                .resetPassword(any());

        mockMvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP"));
    }

    @Test
    @WithAnonymousUser
    void resetPassword_ShouldReturn404_WhenUserNotFoundDuringReset() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest("ghost@mail.com", "123456", "NewPass123!");

        doThrow(new ResourceNotFoundException("User not found")).when(authService)
                .resetPassword(any());

        mockMvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void resetPassword_ShouldReturn400_WhenPasswordIsWeak() throws Exception {

        ResetPasswordRequest req = new ResetPasswordRequest("test@mail.com", "123456", "weak");

        mockMvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
