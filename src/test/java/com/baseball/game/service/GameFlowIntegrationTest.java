package com.baseball.game.service;

import com.baseball.game.dto.GameDto;
import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameFlowIntegrationTest {

    private GameLifecycleServiceImpl lifecycle;
    private GameActionServiceImpl action;
    private GameStateServiceImpl state;

    @BeforeEach
    void setup() throws Exception {
        lifecycle = new GameLifecycleServiceImpl();
        action = new GameActionServiceImpl();
        state = new GameStateServiceImpl();

        // 수동 주입
        java.lang.reflect.Field f;

        f = GameActionServiceImpl.class.getDeclaredField("lifecycleService");
        f.setAccessible(true);
        f.set(action, lifecycle);

        f = GameActionServiceImpl.class.getDeclaredField("stateService");
        f.setAccessible(true);
        f.set(action, state);

        f = GameStateServiceImpl.class.getDeclaredField("lifecycleService");
        f.setAccessible(true);
        f.set(state, lifecycle);

        f = GameStateServiceImpl.class.getDeclaredField("validationService");
        f.setAccessible(true);
        GameValidationService mockValidate = org.mockito.Mockito.mock(GameValidationService.class);
        org.mockito.BDDMockito.given(mockValidate.canChangeInning(org.mockito.ArgumentMatchers.anyString()))
                .willReturn(true);
        f.set(state, mockValidate);
    }

    @Test
    @DisplayName("아웃=3이면 초↔말 전환 또는 이닝 증가, 카운트 초기화")
    void inningSwitchOnThreeOuts() {
        GameDto game = lifecycle.createGame("두산 베어스", "SSG 랜더스", 9, true);
        // current batter/pitcher 보장
        if (game.getCurrentBatter() == null && !game.getAwayBattingOrder().isEmpty()) {
            game.setCurrentBatter(game.getAwayBattingOrder().get(0));
        } else if (game.getCurrentBatter() == null) {
            game.setCurrentBatter(new Batter("최지훈", "SSG 랜더스"));
        }
        if (game.getCurrentPitcher() == null) {
            game.setCurrentPitcher(new Pitcher("곽빈", "두산 베어스"));
        }
        // 기본: 1회 초, 원정 공격
        assertThat(game.isTop()).isTrue();

        // 두 번째 아웃까지 셋업
        game.setOut(3);
        game.setStrike(0);
        game.setBall(0);
        // 다음 타석 처리로 3아웃 유도
        state.nextInning(game.getGameId());

        // 3아웃 후 말로 전환되어야 함
        assertThat(game.isTop()).isFalse();
        assertThat(game.getOut()).isZero();
        assertThat(game.getStrike()).isZero();
        assertThat(game.getBall()).isZero();
    }

    @Test
    @DisplayName("타석 종료 시 타순 진행: currentBatterIndex 증가")
    void advanceBattingOrderOnPlateEnd() {
        GameDto game = lifecycle.createGame("두산 베어스", "SSG 랜더스", 9, true);
        int before = game.getCurrentBatterIndex();
        // 타석 하나 종료되도록 카운트 상태 정리 후 다음 타자로 진행
        game.setStrike(0);
        game.setBall(0);
        state.advanceBattingOrder(game.getGameId());
        assertThat(game.getCurrentBatterIndex()).isNotEqualTo(before);
    }

    @Test
    @DisplayName("타순은 이닝이 넘어가도 팀별로 이어진다")
    void battingOrderContinuesAcrossInnings() {
        GameDto game = lifecycle.createGame("두산 베어스", "SSG 랜더스", 9, true);

        // 원정(SSG) 3번 타자부터 시작했다고 가정
        game.setAwayBatterIndex(2);
        if (!game.getAwayBattingOrder().isEmpty()) {
            game.setCurrentBatterIndex(2);
            game.setCurrentBatter(game.getAwayBattingOrder().get(2));
        }

        // 초→말 전환
        state.nextInning(game.getGameId());
        assertThat(game.isTop()).isFalse();

        // 말→다음 이닝 초 전환 (홈에서 한 타자 진행 후 전환 가정)
        state.advanceBattingOrder(game.getGameId());
        state.nextInning(game.getGameId());

        // 다시 초가 되었을 때, 원정팀 타순은 설정해둔 3번(인덱스 2)에서 이어져야 함
        assertThat(game.isTop()).isTrue();
        assertThat(game.getCurrentBatterIndex()).isEqualTo(game.getAwayBatterIndex());
    }

    @Test
    @DisplayName("offenseTeam/defenseTeam/offenseSide 필드가 현재 상태를 반영")
    void offenseDefenseFieldsReflectState() {
        GameDto game = lifecycle.createGame("두산 베어스", "SSG 랜더스", 9, true);
        // 1회 초: 원정 SSG 공격
        assertThat(game.getOffenseTeam()).isEqualTo("SSG 랜더스");
        assertThat(game.getDefenseTeam()).isEqualTo("두산 베어스");
        assertThat(game.getOffenseSide()).isEqualTo("TOP");

        // 말로 전환
        state.nextInning(game.getGameId());
        assertThat(game.getOffenseTeam()).isEqualTo("두산 베어스");
        assertThat(game.getDefenseTeam()).isEqualTo("SSG 랜더스");
        assertThat(game.getOffenseSide()).isEqualTo("BOTTOM");
    }
}
