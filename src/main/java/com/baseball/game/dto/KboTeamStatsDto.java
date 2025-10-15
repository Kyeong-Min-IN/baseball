package com.baseball.game.dto;

import lombok.Data;

@Data
public class KboTeamStatsDto {
    private String teamName;
    private int gameNum;
    private int win;
    private int lose;
    private int draw;
    private double winPercentage;
    private double gamesBehind;
}
