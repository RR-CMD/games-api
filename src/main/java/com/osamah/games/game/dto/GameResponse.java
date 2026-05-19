package com.osamah.games.game.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record GameResponse(Long id, String title, String description, LocalDate releaseDate, String imageUrl,
                           Integer metacriticScore, Double averageScore, List<String> genres, List<String> platforms,
                           GameStatsResponse stats)implements Serializable {
}