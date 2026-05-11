package com.osamah.games.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(@NotBlank String usernameOrEmail,

                               @NotBlank String password) {
}