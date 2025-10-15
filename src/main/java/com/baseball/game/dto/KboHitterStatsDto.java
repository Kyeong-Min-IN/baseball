package com.baseball.game.dto;

import lombok.Data;

@Data
public class KboHitterStatsDto {
    private String playerName;
    private String playerTeam;
    private double battingAverage;
    private int runsBattedIn;
    private int homeRun;
    private int plateAppearance;
}
