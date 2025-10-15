package com.baseball.game.util;

import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;

/**
 * 야구 통계 계산을 위한 유틸리티 클래스
 */
public class GameStatisticsUtil {

    /**
     * 타자의 타율을 계산합니다.
     * 
     * @param batter 타자 객체
     * @return 계산된 타율 (0.000 ~ 1.000)
     */
    public static double calculateBattingAverage(Batter batter) {
        return batter.getAtBats() > 0 ? (double) batter.getHits() / batter.getAtBats() : 0.0;
    }

    /**
     * 타자의 출루율을 계산합니다.
     * 
     * @param batter 타자 객체
     * @return 계산된 출루율 (0.000 ~ 1.000)
     */
    public static double calculateOnBasePercentage(Batter batter) {
        if (batter.getPlateAppearances() == 0)
            return 0.0;
        return (double) (batter.getHits() + batter.getFourBall() + batter.getHitByPitch()) / batter.getPlateAppearances();
    }

    /**
     * 타자의 장타율을 계산합니다.
     * 
     * @param batter 타자 객체
     * @return 계산된 장타율 (0.000 ~ 4.000)
     */
    public static double calculateSluggingPercentage(Batter batter) {
        if (batter.getAtBats() == 0)
            return 0.0;
        int totalBases = batter.getHits() + (batter.getTwoBases() * 2) +
                (batter.getThreeBases() * 3) + (batter.getHomeRuns() * 4);
        return (double) totalBases / batter.getAtBats();
    }

    /**
     * 타자의 OPS를 계산합니다.
     * 
     * @param batter 타자 객체
     * @return 계산된 OPS
     */
    public static double calculateOPS(Batter batter) {
        return calculateOnBasePercentage(batter) + calculateSluggingPercentage(batter);
    }

    /**
     * 투수의 ERA를 계산합니다.
     * 
     * @param pitcher 투수 객체
     * @return 계산된 ERA
     */
    public static double calculateERA(Pitcher pitcher) {
        return pitcher.getInningsPitched() > 0 ? (pitcher
                .getEarnedRun() * 9.0) / pitcher.getInningsPitched() : 0.0;
    }

    /**
     * 투수의 WHIP를 계산합니다.
     * 
     * @param pitcher 투수 객체
     * @return 계산된 WHIP
     */
    public static double calculateWHIP(Pitcher pitcher) {
        return pitcher.getInningsPitched() > 0
                ? (double) (pitcher.getWalks() + pitcher.getHits()) / pitcher.getInningsPitched()
                : 0.0;
    }

    /**
     * 투수의 삼진율을 계산합니다.
     * 
     * @param pitcher 투수 객체
     * @return 계산된 삼진율 (0.000 ~ 1.000)
     */
    public static double calculateStrikeoutRate(Pitcher pitcher) {
        return pitcher.getTotalBattersFaced() > 0
                ? (double) pitcher.getStrikeouts() / pitcher.getTotalBattersFaced()
                : 0.0;
    }

    /**
     * 투수의 볼넷율을 계산합니다.
     * 
     * @param pitcher 투수 객체
     * @return 계산된 볼넷율 (0.000 ~ 1.000)
     */
    public static double calculateWalkRate(Pitcher pitcher) {
        return pitcher.getTotalBattersFaced() > 0 ? (double) pitcher.getWalks() / pitcher.getTotalBattersFaced()
                : 0.0;
    }

    /**
     * 타자의 삼진율을 계산합니다.
     * 
     * @param batter 타자 객체
     * @return 계산된 삼진율 (0.000 ~ 1.000)
     */
    public static double calculateBatterStrikeoutRate(Batter batter) {
        return batter.getPlateAppearances() > 0 ? (double) batter.getStrikeOut() / batter.getPlateAppearances() : 0.0;
    }

    /**
     * 타자의 볼넷율을 계산합니다.
     * 
     * @param batter 타자 객체
     * @return 계산된 볼넷율 (0.000 ~ 1.000)
     */
    public static double calculateBatterWalkRate(Batter batter) {
        return batter.getPlateAppearances() > 0 ? (double) batter.getFourBall() / batter.getPlateAppearances() : 0.0;
    }
}