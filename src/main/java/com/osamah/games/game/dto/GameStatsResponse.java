package com.osamah.games.game.dto;

import java.io.Serializable;

public record GameStatsResponse(long totalAdded, long plannedCount, long playingCount, long completedCount,
                                long droppedCount) implements Serializable {
}