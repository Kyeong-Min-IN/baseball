package com.baseball.game.service;

import com.baseball.game.dto.GameDto;
import com.baseball.game.dto.Batter;
import com.baseball.game.mapper.MemberMapper;
import com.baseball.game.exception.InvalidGameStateException;
import com.baseball.game.util.GameLogicUtil;
import com.baseball.game.constant.GameConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@Transactional
public class GameStateServiceImpl implements GameStateService {

    private static final Logger log = LoggerFactory.getLogger(GameStateServiceImpl.class);

    @Autowired
    private GameLifecycleService lifecycleService;

    @Autowired
    private GameValidationService validationService;

    @Autowired(required = false)
    private MemberMapper memberMapper; // 로그인/회원 기능이 있는 경우에만 주입

    @Override
    @Transactional
    public GameDto nextInning(String gameId) {
        // 역할: 이닝 전환 시 초/말 토글 및 이닝 증가, 카운트/루상 초기화
        // 전제: 현재 이닝이 종료 가능한 상태인지 별도 검증 서비스에서 확인
        GameDto game = lifecycleService.getGame(gameId);

        if (!validationService.canChangeInning(gameId)) {
            throw new InvalidGameStateException("이닝을 전환할 수 없는 상태입니다.");
        }

        // 이전 공격팀의 타순을 다음 타자로 넘겨준다 (3아웃으로 이닝 종료된 타자는 다음 타석에서 제외)
        boolean wasTop = game.isTop();
        // 야구 규칙: 마지막 이닝(또는 연장) 초가 끝났고 홈이 앞서면 즉시 경기 종료 (말 생략)
        if (wasTop && game.getInning() >= game.getMaxInning() && game.getHomeScore() > game.getAwayScore()) {
            endGame(gameId);
            return game;
        }
        if (wasTop) {
            // 원정팀이 공격을 마침 → awayBatterIndex를 다음 타자로 이동
            List<Batter> away = game.getAwayBattingOrder();
            if (away != null && !away.isEmpty()) {
                int next = (game.getAwayBatterIndex() + 1) % away.size();
                game.setAwayBatterIndex(next);
            }
        } else {
            // 홈팀이 공격을 마침 → homeBatterIndex를 다음 타자로 이동
            List<Batter> home = game.getHomeBattingOrder();
            if (home != null && !home.isEmpty()) {
                int next = (game.getHomeBatterIndex() + 1) % home.size();
                game.setHomeBatterIndex(next);
            }
        }

        if (wasTop) {
            // 초에서 말로: 다음 타석은 홈팀의 이어지는 타자
            game.setTop(false);
        } else {
            // 말에서 다음 이닝 초로: 연장 없음 → 규정 이닝이면 즉시 종료
            if (game.getInning() >= game.getMaxInning()) {
                log.info("규정 이닝 도달로 게임 종료: {}회 말", game.getInning());
                endGame(gameId);
                return game;
            }
            game.setInning(game.getInning() + 1);
            game.setTop(true);
        }

        game.setOut(0);
        game.setStrike(0);
        game.setBall(0);
        GameLogicUtil.resetBases(game);

        // 초/말 전환에 따라 현재 투수/타자를 재설정 (야구 규칙: 타순은 팀별로 이어짐)
        java.util.List<com.baseball.game.dto.Batter> newOffense = game.getCurrentOffensiveLineup();
        if (newOffense != null && !newOffense.isEmpty()) {
            if (game.isTop()) {
                // 원정팀 공격: awayBatterIndex 사용
                int idx = game.getAwayBatterIndex() % newOffense.size();
                game.setCurrentBatterIndex(idx);
                game.setCurrentBatter(newOffense.get(idx));
            } else {
                // 홈팀 공격: homeBatterIndex 사용
                int idx = game.getHomeBatterIndex() % newOffense.size();
                game.setCurrentBatterIndex(idx);
                game.setCurrentBatter(newOffense.get(idx));
            }
        } else {
            game.setCurrentBatter(null);
            game.setCurrentBatterIndex(0);
        }
        // 투수는 수비 팀 선발 투수 유지 (현재 설계상 선발 고정)
        game.setCurrentPitcher(game.getCurrentDefensivePitcher());

        log.info("Inning changed to: {} {}", game.getInning(), game.isTop() ? "초" : "말");

        // 이닝 전환 후 게임 종료 조건 확인
        checkGameOver(gameId);

        return game;
    }

    @Override
    @Transactional
    public GameDto endGame(String gameId) {
        GameDto game = lifecycleService.getGame(gameId);

        log.info("endGame 호출됨 - 게임 ID: {}, 현재 상태: {}회 {}, Home: {} - Away: {}",
                gameId, game.getInning(), game.isTop() ? "초" : "말", game.getHomeScore(), game.getAwayScore());

        game.setGameOver(true);

        if (game.getHomeScore() > game.getAwayScore()) {
            game.setWinner(game.getHomeTeam());
        } else if (game.getAwayScore() > game.getHomeScore()) {
            game.setWinner(game.getAwayTeam());
        } else {
            game.setWinner("무승부");
        }

        log.info("Game ended. Winner: {}, Home: {} - Away: {}", game.getWinner(), game.getHomeScore(),
                game.getAwayScore());

        // 경기 종료 이벤트 기록
        try {
            com.baseball.game.dto.PlayEvent end = com.baseball.game.dto.PlayEvent.builder()
                    .type("GAME_END")
                    .inning(game.getInning())
                    .isTop(game.isTop())
                    .offenseTeam(game.getOffenseTeam())
                    .result("경기 종료")
                    .description("Final: " + game.getHomeTeam() + " " + game.getHomeScore() + " - " + game.getAwayTeam()
                            + " " + game.getAwayScore()
                            + (game.getWinner() != null ? ", Winner: " + game.getWinner() : ""))
                    .out(game.getOut())
                    .strike(game.getStrike())
                    .ball(game.getBall())
                    .homeScore(game.getHomeScore())
                    .homeHit(game.getHomeHit())
                    .homeWalks(game.getHomeWalks())
                    .awayScore(game.getAwayScore())
                    .awayHit(game.getAwayHit())
                    .awayWalks(game.getAwayWalks())
                    .build();
            if (game.getEventLog() == null) {
                game.setEventLog(new java.util.ArrayList<>());
            }
            game.getEventLog().add(end);
        } catch (Exception ignored) {
        }

        // 회원 승/패/무/게임 수 업데이트 (userId를 GameDto에 세팅한 뒤 사용)
        /*
        try {
            if (memberMapper != null && game.getUserId() != null && !game.getUserId().trim().isEmpty()) {
                String userId = game.getUserId();
                memberMapper.incrementGame(userId);

                String winner = game.getWinner();
                if ("무승부".equals(winner)) {
                    memberMapper.incrementDraw(userId);
                } else {
                    boolean userIsAway = game.isUserOffense();
                    String userTeam = userIsAway ? game.getAwayTeam() : game.getHomeTeam();
                    if (userTeam != null && userTeam.equals(winner)) {
                        memberMapper.incrementWin(userId);
                    } else {
                        memberMapper.incrementLose(userId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to update member game result", e);
        }
        */

        return game;
    }

    @Override
    @Transactional
    public void advanceRunners(String gameId, Integer basesToAdvance) {
        GameDto game = lifecycleService.getGame(gameId);
        GameLogicUtil.advanceRunners(game, basesToAdvance);
    }

    @Override
    @Transactional
    public void checkCount(String gameId) {
        // 역할: 스트라이크/볼 누적에 따른 삼진/볼넷 처리와 타순 진행
        GameDto game = lifecycleService.getGame(gameId);

        if (game.getStrike() >= GameConstants.MAX_STRIKES) {
            // 삼진 전 스냅샷 (현재 타자/투수 이름)
            String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
            String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
            // 로그에는 타석 결과 적용 전 아웃카운트를 기록
            int outsBefore = game.getOut();
            game.setOut(outsBefore + 1);
            // 이벤트에 최종 볼카운트를 남기기 위해 리셋 전 값을 보존
            int strikeAtEnd = game.getStrike();
            int ballAtEnd = game.getBall();
            game.setStrike(0);
            game.setBall(0);

            // 이번 경기 스탯 누적: 타자 PA/AB +1, 삼진 +1, 투수 삼진 +1, 투수 이닝(아웃) +1
            try {
                if (batterName != null) {
                    if (game.getBatterGameStatsMap() == null)
                        game.setBatterGameStatsMap(new java.util.HashMap<>());
                    com.baseball.game.dto.BatterGameStats bs = game.getBatterGameStatsMap()
                            .getOrDefault(batterName, com.baseball.game.dto.BatterGameStats.builder()
                                    .playerName(batterName).build());
                    bs.setPlateAppearances(bs.getPlateAppearances() + 1);
                    bs.setAtBats(bs.getAtBats() + 1);
                    bs.setStrikeouts(bs.getStrikeouts() + 1);
                    game.getBatterGameStatsMap().put(batterName, bs);
                }
                if (pitcherName != null) {
                    if (game.getPitcherGameStatsMap() == null)
                        game.setPitcherGameStatsMap(new java.util.HashMap<>());
                    com.baseball.game.dto.PitcherGameStats ps = game.getPitcherGameStatsMap()
                            .getOrDefault(pitcherName, com.baseball.game.dto.PitcherGameStats.builder()
                                    .playerName(pitcherName).build());
                    ps.setStrikeouts(ps.getStrikeouts() + 1);
                    ps.setOutsRecorded(ps.getOutsRecorded() + 1);
                    game.getPitcherGameStatsMap().put(pitcherName, ps);
                }
            } catch (Exception ignored) {
            }

            // 이벤트 로그 추가: 삼진 아웃 (리셋 전 카운트 기록)
            try {
                com.baseball.game.dto.PlayEvent ev = com.baseball.game.dto.PlayEvent.builder()
                        .type("PA_END")
                        .inning(game.getInning())
                        .isTop(game.isTop())
                        .offenseTeam(game.getOffenseTeam())
                        .batter(batterName)
                        .pitcher(pitcherName)
                        .result("삼진 아웃")
                        .description("삼진 아웃")
                        .out(outsBefore)
                        .strike(strikeAtEnd)
                        .ball(ballAtEnd)
                        .homeScore(game.getHomeScore())
                        .awayScore(game.getAwayScore())
                        .build();
                if (game.getEventLog() == null) {
                    game.setEventLog(new java.util.ArrayList<>());
                }
                game.getEventLog().add(ev);
            } catch (Exception ignored) {
            }

            if (game.getOut() < 3) {
                advanceBattingOrder(gameId);
            }
            log.debug("Strikeout. Outs: {}", game.getOut());
        }

        if (game.getBall() >= GameConstants.MAX_BALLS) {
            // 이벤트에 최종 볼카운트를 남기기 위해 리셋 전 값을 보존
            int strikeAtEnd = game.getStrike();
            int ballAtEnd = game.getBall();
            game.setBall(0);
            game.setStrike(0);
            // 볼넷: 강제 주자만 진루 후 타자 1루, 1루가 비어 있으면 타자만 1루
            String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
            String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
            // 득점 전 스냅샷
            int runsBefore = game.isTop() ? game.getAwayScore() : game.getHomeScore();
            GameLogicUtil.processWalk(game, game.getCurrentBatter());
            // 득점 후 계산
            int runsAfter = game.isTop() ? game.getAwayScore() : game.getHomeScore();
            int runsScored = Math.max(0, runsAfter - runsBefore);

            // 팀 볼넷 누적 (현재 공격팀 기준)
            if (game.isTop()) {
                game.setAwayWalks(game.getAwayWalks() + 1);
            } else {
                game.setHomeWalks(game.getHomeWalks() + 1);
            }

            // 이번 경기 스탯 누적: 타자 볼넷 +1 (+타점), 투수 볼넷허용 +1 (+자책)
            try {
                if (batterName != null) {
                    if (game.getBatterGameStatsMap() == null)
                        game.setBatterGameStatsMap(new java.util.HashMap<>());
                    com.baseball.game.dto.BatterGameStats bs = game.getBatterGameStatsMap()
                            .getOrDefault(batterName, com.baseball.game.dto.BatterGameStats.builder()
                                    .playerName(batterName).build());
                    // 볼넷은 타석(Plate Appearance)만 증가, 타수(At-bat)는 증가하지 않음
                    bs.setPlateAppearances(bs.getPlateAppearances() + 1);
                    bs.setWalks(bs.getWalks() + 1);
                    if (runsScored > 0) {
                        bs.setRbis(bs.getRbis() + runsScored);
                    }
                    game.getBatterGameStatsMap().put(batterName, bs);
                }
                if (pitcherName != null) {
                    if (game.getPitcherGameStatsMap() == null)
                        game.setPitcherGameStatsMap(new java.util.HashMap<>());
                    com.baseball.game.dto.PitcherGameStats ps = game.getPitcherGameStatsMap()
                            .getOrDefault(pitcherName, com.baseball.game.dto.PitcherGameStats.builder()
                                    .playerName(pitcherName).build());
                    ps.setWalks(ps.getWalks() + 1);
                    if (runsScored > 0) {
                        ps.setEarnedRunsAllowed(ps.getEarnedRunsAllowed() + runsScored);
                    }
                    game.getPitcherGameStatsMap().put(pitcherName, ps);
                }
            } catch (Exception ignored) {
            }

            // 이벤트 로그: 볼넷 기록 (리셋 전 카운트 기록, PA_END를 먼저 기록)
            try {
                com.baseball.game.dto.PlayEvent ev = com.baseball.game.dto.PlayEvent.builder()
                        .type("PA_END")
                        .inning(game.getInning())
                        .isTop(game.isTop())
                        .offenseTeam(game.getOffenseTeam())
                        .batter(batterName)
                        .pitcher(pitcherName)
                        .result("볼넷")
                        .description("볼넷")
                        .out(game.getOut())
                        .strike(strikeAtEnd)
                        .ball(ballAtEnd)
                        .homeScore(game.getHomeScore())
                        .awayScore(game.getAwayScore())
                        .build();
                if (game.getEventLog() == null) {
                    game.setEventLog(new java.util.ArrayList<>());
                }
                game.getEventLog().add(ev);
            } catch (Exception ignored) {
            }

            // 끝내기 가능성 체크 (마지막 이닝 말에서 홈팀이 앞서면 즉시 종료)
            checkGameOver(gameId);

            if (!game.isGameOver()) {
                advanceBattingOrder(gameId);
            }
            log.debug("Walk. Ball count reset.");
        }
    }

    @Override
    @Transactional
    public void advanceBattingOrder(String gameId) {
        // 역할: 현재 공격 팀의 타순을 한 명 앞으로 진행하고 현재 타자를 갱신
        GameDto game = lifecycleService.getGame(gameId);

        List<Batter> currentLineup = game.getCurrentOffensiveLineup();
        if (currentLineup.isEmpty()) {
            log.warn("No batting order available");
            return;
        }

        // 팀별로 타순 인덱스를 관리 (야구 규칙)
        if (game.isTop()) {
            int next = (game.getAwayBatterIndex() + 1) % currentLineup.size();
            game.setAwayBatterIndex(next);
            game.setCurrentBatterIndex(next);
            game.setCurrentBatter(currentLineup.get(next));
        } else {
            int next = (game.getHomeBatterIndex() + 1) % currentLineup.size();
            game.setHomeBatterIndex(next);
            game.setCurrentBatterIndex(next);
            game.setCurrentBatter(currentLineup.get(next));
        }

        log.debug("Batting order advanced to: {}", game.getCurrentBatterIndex());
    }

    @Override
    @Transactional
    public void checkGameOver(String gameId) {
        // 역할: 규정 이닝 도달/초말 상태에 따른 경기 종료 조건 확인
        GameDto game = lifecycleService.getGame(gameId);

        log.debug("게임 종료 조건 확인: {}회 {}, Home: {} - Away: {}",
                game.getInning(), game.isTop() ? "초" : "말", game.getHomeScore(), game.getAwayScore());

        // 끝내기: 마지막 이닝(이상) 말이고 홈이 앞서는 순간 즉시 종료
        if (game.getInning() >= game.getMaxInning() && !game.isTop()) {
            if (game.getHomeScore() > game.getAwayScore()) {
                log.info("끝내기 조건 만족으로 게임 종료: {}회 말, Home: {} - Away: {}",
                        game.getInning(), game.getHomeScore(), game.getAwayScore());
                endGame(gameId);
                return;
            }
        }

        // 규정 이닝 종료: 9회 말이 끝나면 게임 종료 (3아웃으로 이닝이 끝난 경우)
        if (game.getInning() > game.getMaxInning()) {
            log.info("규정 이닝 초과로 게임 종료: {}회, Home: {} - Away: {}",
                    game.getInning(), game.getHomeScore(), game.getAwayScore());
            endGame(gameId);
            return;
        }

        // 연장은 제외하므로, 위의 조건 외 추가 처리는 불필요
    }

    @Override
    @Transactional
    public void handleScore(String gameId, int score) {
        // 역할: 현재 공격 팀에 점수를 가산
        GameDto game = lifecycleService.getGame(gameId);

        if (game.isTop()) {
            game.setAwayScore(game.getAwayScore() + score);
        } else {
            game.setHomeScore(game.getHomeScore() + score);
        }

        log.info("Score updated. Home: {}, Away: {}", game.getHomeScore(), game.getAwayScore());
        // 점수 갱신 후 종료 조건 확인 (끝내기 포함)
        checkGameOver(gameId);
    }
}