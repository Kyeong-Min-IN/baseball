package com.baseball.ranking.dto;

import com.baseball.game.dto.Player;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class KboHitterStatsDto extends Player {
    private int no;
    private int gameNum;
    private int plateAppearances; // 타석
    private int atBats; // 타수
    private int run; // 득점
    private int hits; // 안타
    private int twoBases; // 2루타
    private int threeBases; // 3루타
    private int homeRuns; // 홈런
    private int totalBases; // 총 루타
    private int runsBattedIn; // 타점
    private int sacrificeBunts; // 희생번트
    private int sacrificeFly; // 희생플라이
    private int fourBall; // 볼넷
    private int ibb; // 고의사구
    private int hitByPitch; // 사구
    private int strikeOut; // 삼진
    private int doubleOut; // 병살타
    private double battingAverage; // 타율
    private double slugging; // 장타율
    private double onBasePercentage; // 출루율
    private double onbasePlusSlug; // OPS
    private int multiHit; // 멀티히트
    private double scoringPositionAvg; // 득점권 타율
    private double pinchHitAvg; // 대타 타율
    private int power;
    private int contact;
    private int speed;
    private int eye;

    public KboHitterStatsDto(String name, String team) {
        this.setName(name);
        this.setTeam(team);
    }
}