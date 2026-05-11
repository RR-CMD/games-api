package com.osamah.games.review.dto;

import jakarta.validation.constraints.*;

public record ReviewPatchRequest(@NotNull(message = "rating is required") @Min(1) @Max(10) Integer rating,

                                 @NotBlank(message = "review cannot be empty") @Size(max = 5000) String content) {
}
