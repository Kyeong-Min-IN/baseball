package com.baseball.game.dto;

import lombok.Data;

@Data
public class KboPitcherStatsDto {
    private String playerName;
    private String playerTeam;
    private double earnedRunAverage;
    private int win;
    private int strikeOut;
    private double inningsPitched;
}
