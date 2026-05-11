package com.osamah.games.usergame.dto;

public record UserGameStatsResponse(int totalPlayed, int planned, int playing, int completed, int dropped) {
}
