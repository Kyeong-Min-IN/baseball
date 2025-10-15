package com.baseball.game.exception;

import com.baseball.game.controller.GameController;
import com.baseball.game.dto.GameActionRequest;
import com.baseball.game.service.GameService;
import com.baseball.game.mapper.BatterMapper;
import com.baseball.game.mapper.PitcherMapper;
import com.baseball.game.mapper.TeamLineupMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baseball.game.mapper.BoardMapper;
import com.baseball.game.mapper.CommentMapper;
import com.baseball.game.mapper.MemberMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GameController.class, excludeAutoConfiguration = { org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class })
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private GameService gameService;

    // 매퍼 스캔으로 인한 의존성 로딩을 차단하기 위해 목으로 대체
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
    // TeamMapper 제거

    @Test
    @DisplayName("ValidationException 발생 시 400 반환")
    void validationException_returns400() throws Exception {
        GameActionRequest req = new GameActionRequest();
        req.setPitchType(null); // pitchType 누락
        mockMvc.perform(post("/api/baseball/game/test-id/pitcher")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("GameNotFoundException 발생 시 404 반환")
    void gameNotFoundException_returns404() throws Exception {
        given(gameService.getGame("not-exist")).willThrow(new GameNotFoundException("게임을 찾을 수 없습니다."));
        mockMvc.perform(get("/api/baseball/game/not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("GAME_NOT_FOUND"));
    }
}
