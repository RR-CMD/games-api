package com.osamah.games.security;


import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetailsService userDetailsService;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldAuthenticateUser_WhenTokenIsValid() throws ServletException, IOException, java.io.IOException {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);
        UserDetails user = new User("osamah@games.com", "pass", Collections.emptyList());

        when(jwtService.extractUsername(token)).thenReturn("osamah@games.com");
        when(userDetailsService.loadUserByUsername("osamah@games.com")).thenReturn(user);
        when(jwtService.isTokenValid(token, user)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext()
                .getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext()
                .getAuthentication()
                .getName()).isEqualTo("osamah@games.com");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSetErrorAttribute_WhenJwtIsInvalid() throws ServletException, IOException, java.io.IOException {

        request.addHeader("Authorization", "Bearer invalid-token");
        when(jwtService.extractUsername(anyString())).thenThrow(new io.jsonwebtoken.MalformedJwtException("Invalid"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(request.getAttribute("jwt_error")).isEqualTo("Client Error: Invalid token provided.");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldHandleExpiredJwtException() throws ServletException, java.io.IOException {
        String token = "expired.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenThrow(
                new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(request.getAttribute("jwt_error")).isEqualTo("Your session has expired. Please log in again.");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldHandleUnexpectedException() throws ServletException, java.io.IOException {

        String token = "any.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Unexpected system failure"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(request.getAttribute("jwt_error")).isEqualTo("An internal security error occurred.");
        verify(filterChain).doFilter(request, response);
    }
}