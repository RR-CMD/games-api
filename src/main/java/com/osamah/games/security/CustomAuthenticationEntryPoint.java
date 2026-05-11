package com.osamah.games.security;

import com.osamah.games.exception.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(@NonNull HttpServletRequest request, HttpServletResponse response,
            @NonNull AuthenticationException ex) throws IOException {

        String jwtError = (String) request.getAttribute("jwt_error");
        String finalMessage = (jwtError != null) ? jwtError : "You must be logged in to access this resource.";

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ApiError error = new ApiError(401, "Unauthorized", finalMessage);

        response.getWriter()
                .write(objectMapper.writeValueAsString(error));
    }
}
