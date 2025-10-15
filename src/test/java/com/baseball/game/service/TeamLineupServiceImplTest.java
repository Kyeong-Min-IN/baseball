package com.baseball.game.service;

import com.baseball.game.dto.Batter;
import com.baseball.game.dto.CustomLineupRequest;
import com.baseball.game.dto.Pitcher;
import com.baseball.game.dto.TeamLineup;
import com.baseball.game.exception.ValidationException;
import com.baseball.game.mapper.BatterMapper;
import com.baseball.game.mapper.PitcherMapper;
import com.baseball.game.mapper.TeamLineupMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class TeamLineupServiceImplTest {

    @InjectMocks
    private TeamLineupServiceImpl service;

    @Mock
    private TeamLineupMapper teamLineupMapper;

    @Mock
    private BatterMapper batterMapper;

    @Mock
    private PitcherMapper pitcherMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service.setBatterMapper(batterMapper);
        service.setPitcherMapper(pitcherMapper);
    }

    @Test
    @DisplayName("saveCustomLineup: 정상적인 요청 시 라인업을 메모리에 저장한다")
    void saveCustomLineup_shouldSaveValidLineupToMemory() {
        // Given
        String userId = "user1";
        String teamName = "Giants";
        CustomLineupRequest req = new CustomLineupRequest();
        req.setUserId(userId);
        req.setTeamName(teamName);

        List<CustomLineupRequest.LineupPosition> lineupPositions = new ArrayList<>();
        // 9명의 타자 설정
        for (int i = 1; i <= 9; i++) {
            String playerName = "Batter " + i;
            CustomLineupRequest.LineupPosition pos = new CustomLineupRequest.LineupPosition();
            pos.setPlayerName(playerName);
            pos.setPosition(i);
            pos.setPlayerNo(i);
            lineupPositions.add(pos);

            // BatterMapper 모의 설정
            Batter mockBatter = new Batter(playerName, teamName);
            given(batterMapper.findByNo(i)).willReturn(mockBatter);
        }
        // 1명의 투수 설정
        String pitcherName = "Pitcher 1";
        CustomLineupRequest.LineupPosition pitcherPos = new CustomLineupRequest.LineupPosition();
        pitcherPos.setPlayerName(pitcherName);
        pitcherPos.setPosition(null); // 투수는 타순이 없음
        pitcherPos.setPlayerNo(10);
        lineupPositions.add(pitcherPos);
        req.setLineup(lineupPositions);

        // PitcherMapper 모의 설정
        Pitcher mockPitcher = new Pitcher(pitcherName, teamName);
        given(pitcherMapper.findByNo(10)).willReturn(mockPitcher);

        // When
        service.saveCustomLineup(req);

        // Then
        // 저장된 커스텀 라인업을 다시 조회하여 확인
        List<TeamLineup> savedLineup = service.getCustomLineup(userId, teamName);
        assertThat(savedLineup).isNotNull();
        assertThat(savedLineup).hasSize(10);
        assertThat(savedLineup.stream().anyMatch(p -> p.getPlayerName().equals(pitcherName) && p.getPosition().equals("Starting_Pitcher"))).isTrue();
        assertThat(savedLineup.stream().anyMatch(p -> p.getPlayerName().equals("Batter 1") && p.getPosition().equals("1th_Batter"))).isTrue();
    }

    @Test
    @DisplayName("saveCustomLineup: 타자가 9명이 아닐 경우 ValidationException 발생")
    void saveCustomLineup_shouldThrowException_whenBatterCountIsNot9() {
        // Given
        String userId = "user2";
        String teamName = "Giants";
        CustomLineupRequest req = new CustomLineupRequest();
        req.setUserId(userId);
        req.setTeamName(teamName);
        List<CustomLineupRequest.LineupPosition> lineup = new ArrayList<>();
        // 8명의 타자 + 1명의 투수
        for (int i = 1; i <= 8; i++) {
            CustomLineupRequest.LineupPosition pos = new CustomLineupRequest.LineupPosition();
            pos.setPlayerName("Batter " + i);
            pos.setPosition(i);
            pos.setPlayerNo(i);
            lineup.add(pos);
            given(batterMapper.findByNo(i)).willReturn(new Batter("Batter " + i, teamName));
        }
        CustomLineupRequest.LineupPosition pitcherPos = new CustomLineupRequest.LineupPosition();
        pitcherPos.setPlayerName("Pitcher 1");
        pitcherPos.setPlayerNo(10);
        lineup.add(pitcherPos);
        req.setLineup(lineup);
        given(pitcherMapper.findByNo(10)).willReturn(new Pitcher("Pitcher 1", teamName));


        // When & Then
        assertThatThrownBy(() -> service.saveCustomLineup(req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("9명의 타자로 정확히 구성되어야 합니다");
    }
}
