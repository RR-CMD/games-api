package com.osamah.games.review.dto;

import java.time.Instant;

public record ReviewForGameResponse(Long id, Long userId, String username, Integer rating, String content,
                                    Instant createdAt, Instant updatedAt) {
}