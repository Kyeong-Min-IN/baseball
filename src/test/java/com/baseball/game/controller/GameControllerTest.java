package com.baseball.game.controller;

import com.baseball.game.dto.GameActionRequest;
import com.baseball.game.dto.GameCreateRequest;
import com.baseball.game.dto.GameDto;
import com.baseball.game.exception.ValidationException;
import com.baseball.game.service.GameService;
import com.baseball.game.service.GameLifecycleService;
import com.baseball.game.service.GameStateService;
import com.baseball.game.service.GameActionService;
import com.baseball.game.service.GameValidationService;
import com.baseball.game.service.TeamLineupService;
import com.baseball.game.mapper.BatterMapper;
import com.baseball.game.mapper.PitcherMapper;
import com.baseball.game.mapper.TeamLineupMapper;
import com.baseball.game.mapper.BoardMapper;
import com.baseball.game.mapper.CommentMapper;
import com.baseball.game.mapper.MemberMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

/**
 * GameController에 대한 단위 테스트.
 * 
 * @WebMvcTest 어노테이션을 사용하여 웹 레이어(컨트롤러)에 대한 테스트에 집중합니다.
 *             GameService는 @MockBean으로 모의 처리되어 컨트롤러의 로직만 순수하게 테스트합니다.
 */
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("GameController 테스트")
class GameControllerTest {

        @Autowired
        private MockMvc mockMvc; // 웹 API를 테스트하기 위한 MockMvc 객체

        @MockBean // 실제 GameService 대신 사용할 Mock 객체
        private GameService gameService;

        // MyBatis 매퍼들이 컨텍스트에 스캔되어 생기는 의존성 문제를 막기 위해 목으로 대체
        @MockBean
        private BatterMapper batterMapper;
        @MockBean
        private PitcherMapper pitcherMapper;
        @MockBean
        private TeamLineupMapper teamLineupMapper;
        @MockBean
        private BoardMapper boardMapper;
        @MockBean
        private CommentMapper commentMapper;
        @MockBean
        private MemberMapper memberMapper;

        @MockBean
        private GameLifecycleService lifecycleService;
        @MockBean
        private GameStateService stateService;
        @MockBean
        private GameActionService actionService;
        @MockBean
        private GameValidationService validationService;
        @MockBean
        private TeamLineupService teamLineupService;
        // TeamMapper 제거: 컨텍스트 충돌 방지용 목 제거

        @Autowired
        private ObjectMapper objectMapper; // Java 객체와 JSON 간의 변환을 담당

        private GameDto sampleGameDto;
        private final String gameId = "test-game-id-12345";

        @BeforeEach
        void setUp() {
                // 각 테스트 실행 전에 공통적으로 사용할 GameDto 객체를 설정합니다.
                sampleGameDto = new GameDto();
                sampleGameDto.setGameId(gameId);
                sampleGameDto.setHomeTeam("Heroes");
                sampleGameDto.setAwayTeam("Bears");
                sampleGameDto.setInning(1);
                sampleGameDto.setHomeScore(0);
                sampleGameDto.setAwayScore(0);
                sampleGameDto.setStrike(0);
                sampleGameDto.setBall(0);
                sampleGameDto.setOut(0);
                // Add dummy Batter and Pitcher for tests that require them
                sampleGameDto.setCurrentBatter(new com.baseball.game.dto.Batter("Test Batter", "Test Team"));
                sampleGameDto.setCurrentPitcher(new com.baseball.game.dto.Pitcher("Test Pitcher", "Test Team"));
        }

        @Test
        @DisplayName("게임 생성 API - 성공")
        void createGame_ShouldReturnCreatedGameDto_WhenRequestIsValid() throws Exception {
                // Given: 유효한 게임 생성 요청 데이터
                GameCreateRequest request = new GameCreateRequest("Heroes", "Bears", 9, true);

                // Mocking: gameService.createGame 메소드가 호출될 때, 미리 정의된 sampleGameDto를 반환하도록 설정
                given(gameService.createGame(anyString(), anyString(), anyInt(), anyBoolean()))
                                .willReturn(sampleGameDto);

                // When: /api/baseball/game 엔드포인트로 POST 요청을 보냄
                ResultActions resultActions = mockMvc.perform(post("/api/baseball/game")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)));

                // Then: 응답을 검증
                resultActions.andExpect(status().isOk()) // HTTP 상태 코드가 200 OK인지 확인
                                .andExpect(jsonPath("$.success").value(true)) // ApiResponse의 success 필드 확인
                                .andExpect(jsonPath("$.data.gameId").value(gameId)) // 응답 데이터의 gameId 확인
                                .andExpect(jsonPath("$.message").value("게임이 생성되었습니다. ID: " + gameId)) // 성공 메시지 확인
                                .andDo(print()); // 요청/응답 전체 내용을 콘솔에 출력
        }

        @Test
        @DisplayName("게임 정보 조회 API - 성공")
        void getGame_ShouldReturnGameDto_WhenGameIdExists() throws Exception {
                // Given: gameService.getGame이 호출될 때 sampleGameDto를 반환하도록 설정
                given(gameService.getGame(gameId)).willReturn(sampleGameDto);

                // When: /api/baseball/game/{gameId} 엔드포인트로 GET 요청을 보냄
                ResultActions resultActions = mockMvc.perform(get("/api/baseball/game/{gameId}", gameId)
                                .accept(MediaType.APPLICATION_JSON));

                // Then: 응답을 검증
                resultActions.andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.gameId").value(gameId))
                                .andExpect(jsonPath("$.data.homeTeam").value("Heroes"))
                                .andDo(print());
        }

        @Test
        @DisplayName("투구 API - 성공 (경량 응답: 라인업 제외)")
        void pitcherThrow_ShouldReturnSlimView_WhenRequestIsValid() throws Exception {
                // Given: 유효한 투구 요청
                GameActionRequest request = new GameActionRequest();
                request.setPitchType("strike");
                request.setSwing(false); // Added to satisfy @NotNull
                String pitchResult = "스트라이크!";

                // Mocking
                given(gameService.pitcherThrow(gameId, request.getPitchType())).willReturn(pitchResult);
                given(gameService.getGame(gameId)).willReturn(sampleGameDto);

                // When
                ResultActions resultActions = mockMvc.perform(post("/api/baseball/game/{gameId}/pitcher", gameId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)));

                // Then
                resultActions.andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.game").exists())
                                .andExpect(jsonPath("$.data.game.gameId").value(gameId))
                                .andExpect(jsonPath("$.message").value("투구 처리 완료: " + pitchResult))
                                // 경량 응답: 라인업 배열은 존재하지 않음
                                .andExpect(jsonPath("$.data.homeBattingOrder").doesNotExist())
                                .andExpect(jsonPath("$.data.awayBattingOrder").doesNotExist())
                                // 편의 필드 존재 확인
                                .andExpect(jsonPath("$.data.offenseTeam").exists())
                                .andExpect(jsonPath("$.data.offenseSide").exists())
                                .andDo(print());
        }

        @Test
        @DisplayName("투구 API - 실패 (투구 유형 누락)")
        void pitcherThrow_ShouldThrowValidationException_WhenPitchTypeIsNull() throws Exception {
                // Given: 투구 유형이 없는 잘못된 요청
                GameActionRequest request = new GameActionRequest();
                request.setPitchType(null); // 또는 request.setPitchType("");

                // When & Then: 컨트롤러 내부에서 ValidationException이 발생하고,
                // @ResponseStatus(HttpStatus.BAD_REQUEST)에 의해 400 에러가 반환되는 것을 기대
                mockMvc.perform(post("/api/baseball/game/{gameId}/pitcher", gameId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andDo(print());
        }

        @Test
        @DisplayName("타격 API - 성공 (경량 응답: 라인업 제외)")
        void batterSwing_ShouldReturnSlimView_WhenRequestIsValid() throws Exception {
                // Given
                GameActionRequest request = new GameActionRequest();
                request.setSwing(true);
                request.setTiming(true); // Changed to Boolean
                request.setPitchType("strike"); // Added to satisfy @NotNull
                String swingResult = "홈런!";

                // Mocking
                given(gameService.batterSwing(anyString(), anyBoolean(), anyBoolean())).willReturn(swingResult);
                sampleGameDto.setCurrentBatter(null);
                sampleGameDto.setCurrentPitcher(null);
                given(gameService.getGame(gameId)).willReturn(sampleGameDto);

                // When
                ResultActions resultActions = mockMvc.perform(post("/api/baseball/game/{gameId}/batter", gameId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)));

                // Then
                resultActions.andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.game").exists())
                                .andExpect(jsonPath("$.data.game.gameId").value(gameId))
                                .andExpect(jsonPath("$.message").value("스윙/노스윙 처리 완료: " + swingResult))
                                // 경량 응답: 라인업 배열은 존재하지 않음
                                .andExpect(jsonPath("$.data.homeBattingOrder").doesNotExist())
                                .andExpect(jsonPath("$.data.awayBattingOrder").doesNotExist())
                                // 현재 타자/투수 키는 존재하며, 값은 null일 수 있음
                                .andExpect(jsonPath("$.data.currentBatter").value(nullValue()))
                                .andExpect(jsonPath("$.data.currentPitcher").value(nullValue()))
                                .andExpect(jsonPath("$.data.offenseTeam").exists())
                                .andDo(print());
        }

        @Test
        @DisplayName("타격 API - 실패 (스윙 결정 누락)")
        void batterSwing_ShouldThrowValidationException_WhenDecisionToSwingIsNull() throws Exception {
                // Given: 스윙 결정 정보가 없는 잘못된 요청
                GameActionRequest request = new GameActionRequest();
                request.setSwing(null);
                request.setTiming(null);

                // When & Then
                mockMvc.perform(post("/api/baseball/game/{gameId}/batter", gameId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andDo(print());
        }

        // 컴퓨터 턴은 이제 pitcherThrow에서 자동 수행되므로 별도 엔드포인트 테스트는 제거합니다.

        @Test
        @DisplayName("다음 이닝 진행 API - 성공")
        void nextInning_ShouldReturnUpdatedGameDto() throws Exception {
                // Given: 다음 이닝으로 변경된 상태의 DTO
                GameDto nextInningDto = new GameDto();
                nextInningDto.setGameId(gameId);
                nextInningDto.setInning(2); // 이닝 증가

                given(gameService.nextInning(gameId)).willReturn(nextInningDto);

                // When
                ResultActions resultActions = mockMvc.perform(post("/api/baseball/game/{gameId}/next-inning", gameId)
                                .contentType(MediaType.APPLICATION_JSON));

                // Then
                resultActions.andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.inning").value(2)) // JSON Path 수정: currentInning -> inning
                                .andExpect(jsonPath("$.message").value(startsWith("다음 이닝으로 진행됩니다.")))
                                .andDo(print());
        }

        // @Test
        // @DisplayName("게임 리셋 API - 성공")
        // void resetGame_ShouldReturnResetGameDto() throws Exception {
        //         // Given
        //         GameDto resetGameDto = new GameDto();
        //         resetGameDto.setGameId(gameId);
        //         resetGameDto.setInning(1);
        //         resetGameDto.setHomeScore(0);
        //         // ... 리셋된 상태의 DTO

        //         // Mocking: service.resetGame()은 void를 반환하므로 특별한 설정이 필요 없음
        //         doNothing().when(gameService).resetGame(gameId);
        //         given(gameService.getGame(gameId)).willReturn(resetGameDto);

        //         // When
        //         ResultActions resultActions = mockMvc.perform(post("/api/baseball/game/{gameId}/reset", gameId)
        //                         .contentType(MediaType.APPLICATION_JSON));

        //         // Then
        //         resultActions.andExpect(status().isOk())
        //                         .andExpect(jsonPath("$.success").value(true))
        //                         .andExpect(jsonPath("$.data.homeScore").value(0))
        //                         .andExpect(jsonPath("$.message").value("게임이 성공적으로 리셋되었습니다."))
        //                         .andDo(print());

        //         // Verify: service.resetGame(gameId)가 정확히 1번 호출되었는지 검증
        //         verify(gameService).resetGame(gameId);
        // }
}
