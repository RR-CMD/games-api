package com.osamah.games.usergame.dto;

import com.osamah.games.usergame.enums.GameStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UserGamePatchRequest(GameStatus gameStatus,

                                   @Min(1) @Max(10) @Schema(nullable = true, example = "null") Integer rating) {
}