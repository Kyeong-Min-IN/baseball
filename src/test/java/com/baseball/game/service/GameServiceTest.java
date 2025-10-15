package com.baseball.game.service;

import com.baseball.game.dto.GameDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("GameServiceImpl 위임 테스트")
class GameServiceImplTest {

    @InjectMocks
    private GameServiceImpl gameService;

    @Mock
    private GameLifecycleService lifecycleService;
    @Mock
    private GameStateService stateService;
    @Mock
    private GameActionService actionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("createGame은 lifecycleService.createGame을 위임한다")
    void createGame_delegatesToLifecycleService() {
        // given
        GameDto dto = new GameDto();
        given(lifecycleService.createGame("A", "B", 9, true)).willReturn(dto);

        // when
        GameDto result = gameService.createGame("A", "B", 9, true);

        // then
        assertThat(result).isSameAs(dto);
        verify(lifecycleService).createGame("A", "B", 9, true);
    }

    @Test
    @DisplayName("getGame은 lifecycleService.getGame을 위임한다")
    void getGame_delegatesToLifecycleService() {
        GameDto dto = new GameDto();
        given(lifecycleService.getGame("id")).willReturn(dto);

        GameDto result = gameService.getGame("id");

        assertThat(result).isSameAs(dto);
        verify(lifecycleService).getGame("id");
    }

    @Test
    @DisplayName("resetGame은 lifecycleService.resetGame을 위임한다")
    void resetGame_delegatesToLifecycleService() {
        // when
        gameService.resetGame("id");

        // then
        verify(lifecycleService).resetGame("id");
    }

    @Test
    @DisplayName("batterSwing(double)은 actionService.batterSwing을 위임한다")
    void batterSwingWithTiming_delegatesToActionService() {
        // given
        given(actionService.batterSwing("id", true, true)).willReturn("result");

        // when
        String result = gameService.batterSwing("id", true, true);

        // then
        assertThat(result).isEqualTo("result");
        verify(actionService).batterSwing("id", true, true);
    }

    @Test
    @DisplayName("pitcherThrow는 actionService.pitcherThrow를 위임한다")
    void pitcherThrow_delegatesToActionService() {
        // given
        given(actionService.pitcherThrow("id", "Fastball")).willReturn("strike");

        // when
        String result = gameService.pitcherThrow("id", "Fastball");

        // then
        assertThat(result).isEqualTo("strike");
        verify(actionService).pitcherThrow("id", "Fastball");
    }

    @Test
    @DisplayName("playComputerTurn은 actionService.playComputerTurn을 위임한다")
    void playComputerTurn_delegatesToActionService() {
        // given
        given(actionService.playComputerTurn("id")).willReturn("AI_Swing");

        // when
        String result = gameService.playComputerTurn("id");

        // then
        assertThat(result).isEqualTo("AI_Swing");
        verify(actionService).playComputerTurn("id");
    }

    @Test
    @DisplayName("nextInning은 stateService.nextInning을 위임한다")
    void nextInning_delegatesToStateService() {
        // given
        GameDto dto = new GameDto();
        given(stateService.nextInning("id")).willReturn(dto);

        // when
        GameDto result = gameService.nextInning("id");

        // then
        assertThat(result).isSameAs(dto);
        verify(stateService).nextInning("id");
    }

    @Test
    @DisplayName("endGame은 stateService.endGame을 위임한다")
    void endGame_delegatesToStateService() {
        // given
        GameDto dto = new GameDto();
        given(stateService.endGame("id")).willReturn(dto);

        // when
        GameDto result = gameService.endGame("id");

        // then
        assertThat(result).isSameAs(dto);
        verify(stateService).endGame("id");
    }

    @Test
    @DisplayName("advanceRunners는 stateService.advanceRunners를 위임한다")
    void advanceRunners_delegatesToStateService() {
        // when
        gameService.advanceRunners("id", 2);

        // then
        verify(stateService).advanceRunners("id", 2);
    }
}
