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
public class KboPitcherStatsDto extends Player {
    private int no;
    private double earnedRunAverage; // 평균자책점
    private int gameNum; // 경기 수
    private int win; // 승
    private int lose; // 패
    private int save; // 세이브
    private int hold; // 홀드
    private double winningPercentage; // 승률
    private double inningsPitched; // 이닝 수
    private int hits; // 피안타
    private int homeRun; // 피홈런
    private int baseOnBalls; // 볼넷
    private int hitByPitch; // 사구
    private int strikeOut; // 삼진
    private int runs; // 실점
    private int earnedRun; // 자책점
    private double whip; // WHIP
    private int completeGame; // 완투
    private int shutout; // 완봉
    private int qualityStart; // 퀄리티 스타트
    private int blownSave; // 블론 세이브
    private int totalBattersFaced; // 상대 타자 수
    private int numberOfPitching; // 투구 수
    private double opponentBattingAverage; // 피안타율
    private int twoBases; // 2루타 허용
    private int threeBases; // 3루타 허용
    private int sacrificeBunt; // 희생번트 허용
    private int sacrificeFly; // 희생플라이 허용
    private int ibb; // 고의사구 허용
    private int wildPitch; // 폭투
    private int balk; // 보크
    private int control;
    private int speed;
    private int stamina;
    private int movement;

    public KboPitcherStatsDto(String name, String team) {
        this.setName(name);
        this.setTeam(team);
    }
}