package com.osamah.games.game.dto;

public record GameStatsResponse(long totalAdded, long plannedCount, long playingCount, long completedCount,
                                long droppedCount) {
}