package com.osamah.games.review.dto;

import java.time.Instant;

public record ReviewForUserResponse(Long id, Long gameId, String gameTitle, String imageUrl, Integer rating,
                                    String content, Instant createdAt, Instant updatedAt) {
}
