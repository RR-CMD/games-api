package com.osamah.games.game.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record GameSummaryResponse(Long id, String title, LocalDate releaseDate, String imageUrl, Double averageScore,
                                  long totalAdded, List<String> genres, List<String> platforms) implements Serializable {
}