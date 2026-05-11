package com.osamah.games.external.rawg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record RawgGameResponse(@JsonProperty("slug") String slug, @JsonProperty("name") String title,
                               @JsonProperty("description_raw") String description, LocalDate released,
                               @JsonProperty("background_image") String imageUrl, Integer metacritic,
                               List<RawgGenre> genres, List<PlatformWrapper> platforms) {

    public record RawgGenre(String name) {
    }

    public record PlatformWrapper(PlatformDetails platform) {
    }

    public record PlatformDetails(String name) {
    }
}
