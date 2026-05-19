package com.osamah.games.usergame.dto;

import java.io.Serializable;

public record UserGameStatsResponse(int totalPlayed, int planned, int playing, int completed,
                                    int dropped) implements Serializable {
}
