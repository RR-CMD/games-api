package com.osamah.games.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(@NotBlank @Email String email,

                                   @NotBlank String otp,

                                   @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$", message =
                                           "Password must be 8-64 characters and contain at least one uppercase " +
                                                   "letter, one lowercase letter, and one number.") String newPassword) {
}