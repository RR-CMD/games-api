package com.osamah.games.usergame.dto;

import com.osamah.games.usergame.enums.GameStatus;

import java.time.Instant;

public record UserGameResponse(Long id, Long gameId, String gameTitle, String imageUrl, GameStatus gameStatus,
                               Integer rating, Instant createdAt, Instant updatedAt) {
}