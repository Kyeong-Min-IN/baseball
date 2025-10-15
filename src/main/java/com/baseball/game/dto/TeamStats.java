package com.baseball.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamStats {
    private int no;
    private String teamName;
    private int gameNum;
    private int win;
    private int lose;
    private int draw;
    private double winPercentage;
    private double gamesBehind;
}
