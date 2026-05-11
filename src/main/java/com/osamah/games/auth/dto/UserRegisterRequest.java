package com.osamah.games.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(@NotBlank @Size(min = 3, max = 20) String username,

                                  @NotBlank @Email String email,

                                  @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$", message =
                                          "Password must be 8-64 characters and contain at least one uppercase " +
                                                  "letter," + " one lowercase letter, and one number.") String password) {
}