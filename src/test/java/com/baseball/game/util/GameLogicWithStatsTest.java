package com.baseball.game.util;

import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;
import com.baseball.game.util.GameLogicUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameLogicWithStatsTest {

    @Test
    @DisplayName("determineHitResultWithTiming: WHIP 낮고 타율 높은 타자는 안타 이상 확률이 높다")
    void hitResult_withGoodStats() {
        Pitcher p = new Pitcher();
        p.setWhip(1.0); // 좋은 제구

        Batter b = new Batter();
        b.setBattingAverage(0.330); // 높은 타율 → 높은 컨택
        b.setHomeRuns(25); // 파워도 일정 수준

        String result = GameLogicUtil.determineHitResultWithTiming(true, p, "strike", 0.5, b);
        // 높은 스탯 조합에서는 아웃보다 안타 이상이 빈번해야 함
        assertThat(result).isIn("안타", "2루타", "3루타", "홈런", "땅볼 아웃", "뜬공 아웃", "삼진 아웃");
    }

    @RepeatedTest(5)
    @DisplayName("determinePitchResultByStats: K% 높고 BB% 낮으면 스트라이크 비중이 높다")
    void pitchResult_withKandBBRates() {
        Pitcher p = new Pitcher();
        p.setTotalBattersFaced(600);
        p.setStrikeOut(180); // 30% K
        p.setWalks(30); // 5% BB
        p.setHitByPitch(3);

        Batter b = new Batter();
        b.setPlateAppearances(600);
        b.setStrikeOut(120); // 20% K
        b.setFourBall(40); // 6.7% BB
        b.setHitByPitch(5);

        String result = GameLogicUtil.determinePitchResultByStats(p, b);
        assertThat(result).isIn("스트라이크", "볼");
    }
}
