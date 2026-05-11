package com.osamah.games.review.dto;

import java.time.Instant;

public record ReviewResponse(Long id, Long userId, String username, Long gameId, String gameTitle, Integer rating,
                             String content, Instant createdAt, Instant updatedAt) {
}
