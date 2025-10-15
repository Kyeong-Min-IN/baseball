package com.baseball.game.util;

import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;
import com.baseball.game.util.GameStatisticsUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameStatisticsUtilTest {

    @Test
    @DisplayName("WHIP 계산: (볼넷+피안타)/이닝")
    void calculateWHIP() {
        Pitcher p = new Pitcher();
        p.setInningsPitched(10);
        p.setWalks(5);
        p.setHits(10);

        double whip = GameStatisticsUtil.calculateWHIP(p);
        assertThat(whip).isEqualTo(1.5); // (5+10)/10
    }

    @Test
    @DisplayName("ERA 계산: (자책*9)/이닝")
    void calculateERA() {
        Pitcher p = new Pitcher();
        p.setInningsPitched(9);
        p.setEarnedRun(5);

        double era = GameStatisticsUtil.calculateERA(p);
        assertThat(era).isEqualTo(5.0);
    }

    @Test
    @DisplayName("타율 계산: 안타/타수")
    void calculateBattingAverage() {
        Batter b = new Batter();
        b.setAtBats(200);
        b.setHits(50);

        double avg = GameStatisticsUtil.calculateBattingAverage(b);
        assertThat(avg).isEqualTo(0.25);
    }
}
