package com.baseball.game.dto;

import lombok.Data;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pitcher extends Player {
    private int no;
    private int gameNum;
    // 순수 실제 성적만 유지
    private int inningsPitched; // 이닝 수
    private int strikeouts; // 삼진
    private int walks; // 볼넷
    private int hits; // 피안타
    private int earnedRun; // 자책점 (earnedRuns -> earnedRun)
    private double earnedRunAverage; // 평균자책점 (era -> earnedRunAverage)
    private double whip; // WHIP (Walks plus Hits per Inning Pitched)
    private int totalBattersFaced; // 상대 타자 수 (pitchersBattersFaced -> totalBattersFaced)
    private int hitByPitch; // 몸에 맞는 볼
    private int win; // 승리
    private int lose; // 패배
    private int save; // 세이브
    private int hold; // 홀드
    private double winningPercentage; // 승률
    private int homeRun; // 피홈런
    private int strikeOut; // 탈삼진
    private int runs; // 실점
    private int completeGame; // 완투
    private int shutout; // 완봉
    private int qualityStart; // 퀄리티 스타트
    private int blownSave; // 블론 세이브
    private int numberOfPitching; // 투구 수
    private double opponentBattingAverage; // 피안타율
    private int twoBases; // 2루타 허용
    private int threeBases; // 3루타 허용
    private int sacrificeBunt; // 희생 번트 허용
    private int sacrificeFly; // 희생 플라이 허용
    private int ibb; // 고의 4구
    private int wildPitch; // 폭투
    private int balk; // 보크
    private int control; // 제구
    private int speed; // 구속
    private int stamina; // 스태미너
    private int movement; // 무브먼트

    public Pitcher(String name, String team) {
        this.setName(name);
        this.setTeam(team);
    }

    // ERA 계산 메서드 (earnedRunAverage 사용)
    public double calculateERA() {
        return inningsPitched > 0 ? (earnedRun * 9.0) / inningsPitched : 0.0;
    }

    // WHIP 계산 메서드
    public double calculateWHIP() {
        return inningsPitched > 0 ? (double) (walks + hits) / inningsPitched : 0.0;
    }

    // 삼진율 계산 메서드
    public double calculateStrikeoutRate() {
        return totalBattersFaced > 0 ? (double) strikeouts / totalBattersFaced : 0.0;
    }

    // 볼넷율 계산 메서드
    public double calculateWalkRate() {
        return totalBattersFaced > 0 ? (double) walks / totalBattersFaced : 0.0;
    }

    // 피안타율 계산 메서드
    public double calculateHitRate() {
        return totalBattersFaced > 0 ? (double) hits / totalBattersFaced : 0.0;
    }
}
