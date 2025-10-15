package com.baseball.game.util;

import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;
import com.baseball.game.dto.GameDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class GameLogicUtilTest {
    @Test
    @DisplayName("determinePitchResultByStats: 스트라이크/볼 확률 계산")
    void determinePitchResultByStats() {
        Pitcher pitcher = new Pitcher();
        pitcher.setTotalBattersFaced(100);
        pitcher.setStrikeOut(30);
        pitcher.setWalks(10);
        pitcher.setHitByPitch(2);
        Batter batter = new Batter();
        batter.setPlateAppearances(100);
        batter.setStrikeOut(20);
        batter.setFourBall(15);
        batter.setHitByPitch(1);
        String result = GameLogicUtil.determinePitchResultByStats(pitcher, batter);
        assertThat(result).isIn("스트라이크", "볼");
    }

    @Test
    @DisplayName("calculateContactFromBattingAverage: 타율별 컨택 계산")
    void calculateContactFromBattingAverage() {
        assertThat(GameLogicUtil.calculateContactFromBattingAverage(0.150)).isEqualTo(50);
        assertThat(GameLogicUtil.calculateContactFromBattingAverage(0.350)).isEqualTo(87);
        assertThat(GameLogicUtil.calculateContactFromBattingAverage(0.250)).isBetween(50, 100);
    }

    @Test
    @DisplayName("determinePitchResult: 제구력에 따른 투구 결과")
    void determinePitchResult() {
        Pitcher pitcher = new Pitcher();
        pitcher.setWhip(1.0); // 좋은 제구력
        String result = GameLogicUtil.determinePitchResult(pitcher, "strike");
        assertThat(result).isIn("스트라이크", "볼");
    }

    @Test
    @DisplayName("determineHitResultWithTiming: 타격 결과 계산")
    void determineHitResultWithTiming() {
        Pitcher pitcher = new Pitcher();
        pitcher.setWhip(1.0);
        Batter batter = new Batter();
        batter.setBattingAverage(0.300);
        batter.setHomeRuns(20);
        String result = GameLogicUtil.determineHitResultWithTiming(true, pitcher, "strike", 0.5, batter);
        // 이 테스트는 확률적 결과를 반환하므로, 결과가 null이 아님을 확인하는 정도로만 검증합니다.
        // 특정 결과(안타, 아웃 등)를 강제하기는 어렵습니다.
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("resetBases/advanceRunners/addRunnerToBase: 베이스/주자 처리")
    void baseAndRunnerLogic() {
        GameDto game = new GameDto();
        Batter batter1 = new Batter();
        Batter batter2 = new Batter();
        GameLogicUtil.resetBases(game);
        GameLogicUtil.addRunnerToBase(game, 1, batter1);
        GameLogicUtil.addRunnerToBase(game, 2, batter2);
        assertThat(game.getBases()[1]).isEqualTo(batter1);
        assertThat(game.getBases()[2]).isEqualTo(batter2);
        GameLogicUtil.advanceRunners(game, 1);
        assertThat(game.getBases()[2]).isEqualTo(batter1);
        assertThat(game.getBases()[3]).isEqualTo(batter2);
    }

    @Test
    @DisplayName("processGroundBall: 땅볼 처리")
    void processGroundBall() {
        GameDto game = new GameDto();
        Batter batter = new Batter();
        GameLogicUtil.resetBases(game);
        game.setOut(1);
        String result = GameLogicUtil.processGroundBall(game, batter);
        assertThat(result).startsWith("땅볼 아웃");
    }
}

// 추가 테스트
class GameLogicUtilExtraTest {
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("resetBases: 베이스와 주자 리스트 초기화")
    void resetBases_clears() {
        GameDto game = new GameDto();
        game.setBases(new Batter[] { new Batter(), new Batter(), new Batter(), new Batter() });
        
        GameLogicUtil.resetBases(game);
        org.assertj.core.api.Assertions.assertThat(game.getBases()).containsOnlyNulls();
        
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("addRunnerToBase: 지정 베이스에 주자 추가 및 리스트 반영")
    void addRunner_adds() {
        GameDto game = new GameDto();
        GameLogicUtil.resetBases(game);
        Batter runner = new Batter();
        runner.setName("Runner1");
        GameLogicUtil.addRunnerToBase(game, 1, runner);
        org.assertj.core.api.Assertions.assertThat(game.getBases()[1]).isEqualTo(runner);
        
    }
}