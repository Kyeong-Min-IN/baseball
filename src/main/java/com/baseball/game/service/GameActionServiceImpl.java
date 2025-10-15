package com.baseball.game.service;

import com.baseball.game.dto.GameDto;
import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;
import com.baseball.game.exception.InvalidGameStateException;
import com.baseball.game.exception.ValidationException;
import com.baseball.game.util.GameLogicUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class GameActionServiceImpl implements GameActionService {

    private static final Logger log = LoggerFactory.getLogger(GameActionServiceImpl.class);

    @Autowired
    private GameLifecycleService lifecycleService;

    @Autowired
    private GameStateService stateService;

    @Override
    @Transactional
    public String batterSwing(String gameId, Boolean swing, Boolean timing) {
        // 역할: 한 번의 스윙 액션을 처리하여 카운트/루상/점수 등의 게임 상태를 갱신
        // 전제 조건: 유효한 게임, 현재 타자/투수가 세팅되어 있어야 함
        GameDto game = lifecycleService.getGame(gameId);

        if (game.isGameOver()) {
            throw new InvalidGameStateException("게임이 이미 종료되었습니다.");
        }
        // 유저 공격 턴 확인: 유저가 공격 팀이고 현재 초(away)거나, 유저가 수비 팀이면 현재 말(home)일 때만 스윙 허용
        boolean isUserOffenseNow = (game.isUserOffense() && game.isTop()) || (!game.isUserOffense() && !game.isTop());
        if (!isUserOffenseNow) {
            throw new InvalidGameStateException("유저의 공격 턴이 아닙니다.");
        }
        if (game.getCurrentBatter() == null || game.getCurrentPitcher() == null) {
            throw new InvalidGameStateException("현재 타자 또는 투수가 설정되지 않았습니다. 라인업을 먼저 설정해주세요.");
        }
        if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
            throw new InvalidGameStateException("현재 공격 이닝이 종료되었습니다. 다음 이닝으로 진행해주세요.");
        }
        return doSwingHuman(gameId, swing, timing);
    }

    @Override
    @Transactional
    public String pitcherThrow(String gameId, String pitchType) {
        // 역할: 투수의 한 번의 투구 결과(스트라이크/볼)를 결정하고 카운트를 갱신
        // 설계: 타자/투수의 기록 기반 확률 모델을 사용하여 결과 도출
        GameDto game = lifecycleService.getGame(gameId);

        if (game.isGameOver()) {
            throw new InvalidGameStateException("게임이 이미 종료되었습니다.");
        }
        // 유저 수비 턴 확인: 유저가 수비일 때만 투구 가능
        boolean isUserOffenseNow = (game.isUserOffense() && game.isTop()) || (!game.isUserOffense() && !game.isTop());
        boolean isUserDefenseNow = !isUserOffenseNow;
        if (!isUserDefenseNow) {
            throw new InvalidGameStateException("유저의 수비 턴이 아닙니다.");
        }
        if (game.getCurrentBatter() == null || game.getCurrentPitcher() == null) {
            throw new InvalidGameStateException("현재 타자 또는 투수가 설정되지 않았습니다. 라인업을 먼저 설정해주세요.");
        }
        if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
            throw new InvalidGameStateException("현재 공격 이닝이 종료되었습니다. 다음 이닝으로 진행해주세요.");
        }

        if (pitchType == null) {
            throw new ValidationException("투구 유형을 지정해주세요.");
        }
        String intended = pitchType.trim().toLowerCase();
        if (!"strike".equals(intended) && !"ball".equals(intended)) {
            throw new ValidationException("투구 유형은 'strike' 또는 'ball' 이어야 합니다.");
        }

        String pitchResult = GameLogicUtil.determinePitchResult(game.getCurrentPitcher(), intended);
        // 투구수 1 증가 (현재 투수)
        incrementPitchCount(game, game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null, 1);

        log.info("게임 {}: 투수 {} 투구. 결과: {}",
                gameId, game.getCurrentPitcher().getName(), pitchResult);

        // 현재 턴이 컴퓨터 공격인지 선 판별 (중복 카운트 방지의 핵심 분기)
        boolean isComputerOffense = (game.isUserOffense() && !game.isTop()) || (!game.isUserOffense() && game.isTop());
        if (isComputerOffense) {
            try {
                String pitchZoneValue = "스트라이크".equals(pitchResult) ? "strike" : "ball";
                String computerResult = playComputerTurn(gameId, pitchZoneValue);
                // 컴퓨터가 스윙하지 않은 경우에만 투구 결과를 카운트에 반영
                if ("스윙 안함".equals(computerResult)) {
                    switch (pitchResult) {
                        case "스트라이크":
                            game.setStrike(game.getStrike() + 1);
                            break;
                        case "볼":
                            game.setBall(game.getBall() + 1);
                            break;
                    }
                    stateService.checkCount(gameId);
                    if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                        stateService.nextInning(gameId);
                    }
                } else {
                    // 컴퓨터가 스윙하여 타구가 처리된 경우에도 카운트 일관성 유지를 위해 checkCount 1회 호출
                    stateService.checkCount(gameId);
                }
                return pitchResult + " | 컴퓨터 타격: " + computerResult;
            } catch (Exception e) {
                log.warn("컴퓨터 타격 자동 진행 중 오류 발생", e);
                // 오류 시 기본 투구 결과를 반영하여 게임 진행 유지
                switch (pitchResult) {
                    case "스트라이크":
                        game.setStrike(game.getStrike() + 1);
                        break;
                    case "볼":
                        game.setBall(game.getBall() + 1);
                        break;
                }
                stateService.checkCount(gameId);
                if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                    stateService.nextInning(gameId);
                }
                // 게임 종료 조건 확인 (끝내기 등)
                stateService.checkGameOver(gameId);
                return pitchResult;
            }
        } else {
            // 유저 공격 턴: 기존처럼 투구 결과를 즉시 반영
            switch (pitchResult) {
                case "스트라이크":
                    game.setStrike(game.getStrike() + 1);
                    // 삼진/볼넷은 checkCount에서 처리되므로 여기서는 투구수만 누적됨
                    break;
                case "볼":
                    game.setBall(game.getBall() + 1);
                    break;
            }
            // 카운트 검사: 삼진/볼넷 처리 및 타순 진행
            stateService.checkCount(gameId);
            // 삼진으로 아웃 누적이 3 이상이면 이닝 전환
            if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                stateService.nextInning(gameId);
            }
            // 게임 종료 조건 확인 (끝내기 등)
            stateService.checkGameOver(gameId);
            return pitchResult;
        }
    }

    @Override
    public String playComputerTurn(String gameId) {
        return playComputerTurn(gameId, null);
    }

    public String playComputerTurn(String gameId, String forcedPitchZone) {
        GameDto game = lifecycleService.getGame(gameId);

        boolean isComputerOffense = (game.isUserOffense() && !game.isTop()) || (!game.isUserOffense() && game.isTop());

        if (!isComputerOffense) {
            return "유저의 턴입니다.";
        }

        Batter computerBatter = game.getCurrentBatter();
        Pitcher currentPitcher = game.getCurrentPitcher();

        double batterStrikeoutRate = computerBatter.getStrikeOut()
                / (double) (computerBatter.getPlateAppearances() > 0 ? computerBatter.getPlateAppearances() : 1);
        double pitcherStrikeoutRate = currentPitcher.getStrikeouts()
                / (double) (currentPitcher.getTotalBattersFaced() > 0 ? currentPitcher.getTotalBattersFaced()
                        : 1);

        double swingProbability = 0.5 + (batterStrikeoutRate - pitcherStrikeoutRate);
        swingProbability = Math.max(0.2, Math.min(0.9, swingProbability));

        boolean decisionToSwing = (Math.random() < swingProbability);

        // 노스윙이면 이미 pitcherThrow에서 카운트를 처리했으므로 추가 처리하지 않음
        if (!decisionToSwing) {
            return "스윙 안함";
        }

        int contactStat = GameLogicUtil.calculateContactFromBattingAverage(computerBatter.getBattingAverage());
        double timing;

        double timingRange = 0.5 - (contactStat / 200.0);
        timing = 0.5 + (Math.random() - 0.5) * timingRange;
        timing = Math.max(0.1, Math.min(0.9, timing));

        // 강제 존이 있으면 해당 존으로 스윙 처리
        if (forcedPitchZone != null) {
            return doSwingWithZone(gameId, true, timing, forcedPitchZone);
        }
        return doSwing(gameId, true, timing);
    }

    private String doSwing(String gameId, Boolean swing, Double timing) {
        GameDto game = lifecycleService.getGame(gameId);
        String pitchZone;
        String zoneK = GameLogicUtil.determinePitchResultByStats(game.getCurrentPitcher(), game.getCurrentBatter());
        pitchZone = "스트라이크".equals(zoneK) ? "strike" : "ball";

        String hitResult = GameLogicUtil.determineHitResultWithTiming(
                swing,
                game.getCurrentPitcher(),
                pitchZone,
                timing,
                game.getCurrentBatter());

        log.info("게임 {}: 타자 {} (타이밍: {}) 스윙: {}, 투수 {} 투구 결과: {}, 타격 결과: {}",
                gameId, game.getCurrentBatter().getName(), timing, swing, game.getCurrentPitcher().getName(),
                pitchZone, hitResult);

        switch (hitResult) {
            case "스트라이크":
                game.setStrike(game.getStrike() + 1);
                stateService.checkCount(gameId);
                // 스윙 유무와 관계없이 삼진으로 3아웃이 되면 이닝 전환
                if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                    stateService.nextInning(gameId);
                }
                break;
            case "볼":
                game.setBall(game.getBall() + 1);
                stateService.checkCount(gameId);
                if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                    stateService.nextInning(gameId);
                }
                break;
            case "헛스윙":
                game.setStrike(game.getStrike() + 1);
                stateService.checkCount(gameId);
                if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                    stateService.nextInning(gameId);
                }
                break;
            case "안타":
            case "2루타":
            case "3루타": {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int basesToAdvance = 0;
                if (hitResult.equals("안타"))
                    basesToAdvance = 1;
                else if (hitResult.equals("2루타"))
                    basesToAdvance = 2;
                else if (hitResult.equals("3루타"))
                    basesToAdvance = 3;
                // 득점 전 스냅샷
                int runsBefore = getOffenseScore(game);
                GameLogicUtil.advanceRunners(game, basesToAdvance);
                GameLogicUtil.addRunnerToBase(game, basesToAdvance, game.getCurrentBatter());
                // 득점 후 계산
                int runsAfter = getOffenseScore(game);
                int runsScored = Math.max(0, runsAfter - runsBefore);
                // 스탯 누적: 타자 PA/AB/H, 투수 피안타
                accumulateBatterPaAbHit(game, batterName, true, true, true);
                incrementPitcherHitAllowed(game, pitcherName);
                // 팀 히트 누적 (현재 공격팀 기준) - 중복 방지하여 1회만 증가
                incrementTeamHit(game);
                // 타점/자책점 누적
                if (runsScored > 0) {
                    incrementBatterRbi(game, batterName, runsScored);
                    incrementPitcherEarnedRuns(game, pitcherName, runsScored);
                }
                addPlayEvent(game, "PA_END", hitResult, batterName, pitcherName, "타구 결과: " + hitResult);
                game.setStrike(0);
                game.setBall(0);
                stateService.checkGameOver(gameId);
                if (game.getOut() < 3 && !game.isGameOver()) {
                    stateService.advanceBattingOrder(gameId);
                }
                break;
            }
            case "홈런": {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int runs = 1;
                com.baseball.game.dto.Batter[] bases = game.getBases();
                if (bases != null) {
                    if (bases[1] != null)
                        runs++;
                    if (bases[2] != null)
                        runs++;
                    if (bases[3] != null)
                        runs++;
                }
                if (game.isTop())
                    game.setAwayScore(game.getAwayScore() + runs);
                else
                    game.setHomeScore(game.getHomeScore() + runs);
                // 타점/자책점 누적
                incrementBatterRbi(game, batterName, runs);
                incrementPitcherEarnedRuns(game, pitcherName, runs);
                // 팀 히트 누적 (홈런도 히트 1)
                incrementTeamHit(game);
                GameLogicUtil.resetBases(game);
                addPlayEvent(game, "PA_END", "홈런", batterName, pitcherName, "홈런으로 " + runs + "득점");
                game.setStrike(0);
                game.setBall(0);
                stateService.checkGameOver(gameId);
                if (game.getOut() < 3 && !game.isGameOver()) {
                    stateService.advanceBattingOrder(gameId);
                }
                // 스탯 누적: 타자 PA/AB/H + HR, 투수 피안타/피홈런
                accumulateBatterPaAbHit(game, batterName, true, true, true);
                incrementBatterHomer(game, batterName);
                incrementPitcherHitAllowed(game, pitcherName);
                incrementPitcherHomerAllowed(game, pitcherName);
                break;
            }
            case "뜬공 아웃":
            case "삼진 아웃":
            case "땅볼 아웃":
            case "병살타": {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int beforeOuts = game.getOut();
                int runsBefore = getOffenseScore(game);
                if ("땅볼 아웃".equals(hitResult) || "병살타".equals(hitResult)) {
                    String gbResult = GameLogicUtil.processGroundBall(game, game.getCurrentBatter());
                    // 실제 처리 결과(병살타 등)로 결과 문자열 업데이트
                    hitResult = gbResult;
                }
                if ("삼진 아웃".equals(hitResult) || "뜬공 아웃".equals(hitResult)) {
                    game.setOut(beforeOuts + 1);
                }
                // 득점 후 계산 (땅볼 처리로 인한 강제 득점 반영)
                int runsAfter = getOffenseScore(game);
                int runsScored = Math.max(0, runsAfter - runsBefore);
                // outsGained는 이닝 전환 전에 계산해야 리셋 영향이 없음
                int outsGained = game.getOut() - beforeOuts;
                addPlayEvent(game, "PA_END", hitResult, batterName, pitcherName, "아웃: " + hitResult, beforeOuts);
                game.setStrike(0);
                game.setBall(0);
                if (game.getOut() >= 3)
                    stateService.nextInning(gameId);
                else
                    stateService.advanceBattingOrder(gameId);
                // 스탯 누적: 타석 + 타수, 삼진이면 삼진 증가, 투수 아웃 수 증가
                accumulateBatterPaAbHit(game, batterName, true, true, false);
                if ("삼진 아웃".equals(hitResult)) {
                    incrementBatterStrikeout(game, batterName);
                    incrementPitcherStrikeout(game, pitcherName);
                }
                if (outsGained > 0) {
                    incrementPitcherOut(game, pitcherName, outsGained);
                }
                // 타점/자책점 누적 (강제 득점 발생 시)
                if (runsScored > 0) {
                    incrementBatterRbi(game, batterName, runsScored);
                    incrementPitcherEarnedRuns(game, pitcherName, runsScored);
                }
                break;
            }
            default: {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int beforeOuts = game.getOut();
                game.setOut(beforeOuts + 1);
                int outsGained = game.getOut() - beforeOuts; // 전환 전 계산
                addPlayEvent(game, "PA_END", hitResult, batterName, pitcherName, "타석 종료: " + hitResult, beforeOuts);
                game.setStrike(0);
                game.setBall(0);
                if (game.getOut() >= 3)
                    stateService.nextInning(gameId);
                else
                    stateService.advanceBattingOrder(gameId);
                // 스탯 누적: 일반 아웃 → 타석+타수
                accumulateBatterPaAbHit(game, batterName, true, true, false);
                if (outsGained > 0) {
                    incrementPitcherOut(game, pitcherName, outsGained);
                }
                break;
            }
        }
        return hitResult;
    }

    private String doSwingWithZone(String gameId, Boolean swing, Double timing, String pitchZone) {
        GameDto game = lifecycleService.getGame(gameId);
        String hitResult = GameLogicUtil.determineHitResultWithTiming(
                swing,
                game.getCurrentPitcher(),
                pitchZone,
                timing,
                game.getCurrentBatter());

        log.info("게임 {}: 타자 {} (타이밍: {}) 스윙: {}, 강제존: {}, 타격 결과: {}",
                gameId, game.getCurrentBatter().getName(), timing, swing, pitchZone, hitResult);

        // 아래 로직은 doSwing과 동일하게 처리
        switch (hitResult) {
            case "스트라이크":
                game.setStrike(game.getStrike() + 1);
                stateService.checkCount(gameId);
                if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                    stateService.nextInning(gameId);
                }
                break;
            case "볼":
                game.setBall(game.getBall() + 1);
                stateService.checkCount(gameId);
                if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                    stateService.nextInning(gameId);
                }
                break;
            case "헛스윙":
                game.setStrike(game.getStrike() + 1);
                stateService.checkCount(gameId);
                if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                    stateService.nextInning(gameId);
                }
                break;
            case "안타":
            case "2루타":
            case "3루타": {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int basesToAdvance = 0;
                if (hitResult.equals("안타"))
                    basesToAdvance = 1;
                else if (hitResult.equals("2루타"))
                    basesToAdvance = 2;
                else if (hitResult.equals("3루타"))
                    basesToAdvance = 3;
                int runsBefore = getOffenseScore(game);
                GameLogicUtil.advanceRunners(game, basesToAdvance);
                GameLogicUtil.addRunnerToBase(game, basesToAdvance, game.getCurrentBatter());
                int runsAfter = getOffenseScore(game);
                int runsScored = Math.max(0, runsAfter - runsBefore);
                accumulateBatterPaAbHit(game, batterName, true, true, true);
                incrementPitcherHitAllowed(game, pitcherName);
                // 팀 히트 누적
                incrementTeamHit(game);
                if (runsScored > 0) {
                    incrementBatterRbi(game, batterName, runsScored);
                    incrementPitcherEarnedRuns(game, pitcherName, runsScored);
                }
                game.setStrike(0);
                game.setBall(0);
                addPlayEvent(game, "PA_END", hitResult, batterName, pitcherName, "타구 결과: " + hitResult);
                stateService.checkGameOver(gameId);
                if (game.getOut() < 3 && !game.isGameOver()) {
                    stateService.advanceBattingOrder(gameId);
                }
                break;
            }
            case "홈런": {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int runs = 1;
                com.baseball.game.dto.Batter[] bases = game.getBases();
                if (bases != null) {
                    if (bases[1] != null)
                        runs++;
                    if (bases[2] != null)
                        runs++;
                    if (bases[3] != null)
                        runs++;
                }
                if (game.isTop())
                    game.setAwayScore(game.getAwayScore() + runs);
                else
                    game.setHomeScore(game.getHomeScore() + runs);
                incrementBatterRbi(game, batterName, runs);
                incrementPitcherEarnedRuns(game, pitcherName, runs);
                // 팀 히트 누적 (홈런도 히트 1)
                if (game.isTop()) {
                    game.setAwayHit(game.getAwayHit() + 1);
                } else {
                    game.setHomeHit(game.getHomeHit() + 1);
                }
                GameLogicUtil.resetBases(game);
                game.setStrike(0);
                game.setBall(0);
                addPlayEvent(game, "PA_END", "홈런", batterName, pitcherName, "홈런으로 " + runs + "득점");
                stateService.checkGameOver(gameId);
                if (game.getOut() < 3 && !game.isGameOver()) {
                    stateService.advanceBattingOrder(gameId);
                }
                accumulateBatterPaAbHit(game, batterName, true, true, true);
                incrementBatterHomer(game, batterName);
                incrementPitcherHitAllowed(game, pitcherName);
                incrementPitcherHomerAllowed(game, pitcherName);
                break;
            }
            case "뜬공 아웃":
            case "삼진 아웃":
            case "땅볼 아웃":
            case "병살타": {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int beforeOuts = game.getOut();
                int runsBefore = getOffenseScore(game);
                if ("땅볼 아웃".equals(hitResult) || "병살타".equals(hitResult)) {
                    String gbResult = GameLogicUtil.processGroundBall(game, game.getCurrentBatter());
                    hitResult = gbResult;
                }
                if ("삼진 아웃".equals(hitResult) || "뜬공 아웃".equals(hitResult)) {
                    game.setOut(beforeOuts + 1);
                }
                int runsAfter = getOffenseScore(game);
                int runsScored = Math.max(0, runsAfter - runsBefore);
                int outsGained = game.getOut() - beforeOuts;
                game.setStrike(0);
                game.setBall(0);
                addPlayEvent(game, "PA_END", hitResult, batterName, pitcherName, "아웃: " + hitResult, beforeOuts);
                if (game.getOut() >= 3)
                    stateService.nextInning(gameId);
                else
                    stateService.advanceBattingOrder(gameId);
                accumulateBatterPaAbHit(game, batterName, true, true, false);
                if ("삼진 아웃".equals(hitResult)) {
                    incrementBatterStrikeout(game, batterName);
                    incrementPitcherStrikeout(game, pitcherName);
                }
                if (outsGained > 0) {
                    incrementPitcherOut(game, pitcherName, outsGained);
                }
                if (runsScored > 0) {
                    incrementBatterRbi(game, batterName, runsScored);
                    incrementPitcherEarnedRuns(game, pitcherName, runsScored);
                }
                break;
            }
            default: {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int beforeOuts = game.getOut();
                game.setOut(beforeOuts + 1);
                int outsGained = game.getOut() - beforeOuts;
                addPlayEvent(game, "PA_END", hitResult, batterName, pitcherName, "타석 종료: " + hitResult, beforeOuts);
                game.setStrike(0);
                game.setBall(0);
                if (game.getOut() >= 3)
                    stateService.nextInning(gameId);
                else
                    stateService.advanceBattingOrder(gameId);
                accumulateBatterPaAbHit(game, batterName, true, true, false);
                if (outsGained > 0) {
                    incrementPitcherOut(game, pitcherName, outsGained);
                }
                break;
            }
        }
        return hitResult;
    }

    private String doSwingHuman(String gameId, Boolean swing, Boolean timingBonus) {
        GameDto game = lifecycleService.getGame(gameId);
        // 컴퓨터 투수의 투구 1회로 간주하여 투구수 +1
        incrementPitchCount(game, game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null, 1);
        boolean isUserOffenseNow = (game.isUserOffense() && game.isTop()) || (!game.isUserOffense() && !game.isTop());
        String pitchZone;
        String zoneK = GameLogicUtil.determinePitchResultByStats(game.getCurrentPitcher(), game.getCurrentBatter());
        pitchZone = "스트라이크".equals(zoneK) ? "strike" : "ball";

        String hitResult = GameLogicUtil.determineHitResultWithTiming(
                swing,
                game.getCurrentPitcher(),
                pitchZone,
                timingBonus != null && timingBonus,
                game.getCurrentBatter());

        log.info("게임 {}: 타자 {} (타이밍보너스: {}) 스윙: {}, 투수 {} 투구 결과: {}, 타격 결과: {}",
                gameId, game.getCurrentBatter().getName(), timingBonus, swing, game.getCurrentPitcher().getName(),
                pitchZone, hitResult);

        switch (hitResult) {
            case "스트라이크":
                game.setStrike(game.getStrike() + 1);
                stateService.checkCount(gameId);
                if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                    stateService.nextInning(gameId);
                }
                // 삼진 처리 시 투수/타자 스탯은 checkCount 후 doSwingHuman 분기에서 일괄 처리
                break;
            case "볼":
                game.setBall(game.getBall() + 1);
                stateService.checkCount(gameId);
                if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                    stateService.nextInning(gameId);
                }
                break;
            case "헛스윙":
                game.setStrike(game.getStrike() + 1);
                stateService.checkCount(gameId);
                if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
                    stateService.nextInning(gameId);
                }
                break;
            case "안타":
            case "2루타":
            case "3루타": {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int basesToAdvance = 0;
                if (hitResult.equals("안타"))
                    basesToAdvance = 1;
                else if (hitResult.equals("2루타"))
                    basesToAdvance = 2;
                else if (hitResult.equals("3루타"))
                    basesToAdvance = 3;
                // 득점 전/후 스냅샷으로 타점/자책 산출
                int runsBefore = getOffenseScore(game);
                GameLogicUtil.advanceRunners(game, basesToAdvance);
                GameLogicUtil.addRunnerToBase(game, basesToAdvance, game.getCurrentBatter());
                int runsAfter = getOffenseScore(game);
                int runsScored = Math.max(0, runsAfter - runsBefore);
                // 스탯 누적: 타자 PA/AB/H, 투수 피안타
                accumulateBatterPaAbHit(game, batterName, true, true, true);
                incrementPitcherHitAllowed(game, pitcherName);
                // 팀 히트 누적 (현재 공격팀 기준)
                if (game.isTop()) {
                    game.setAwayHit(game.getAwayHit() + 1);
                } else {
                    game.setHomeHit(game.getHomeHit() + 1);
                }
                // 타점/자책점 누적
                if (runsScored > 0) {
                    incrementBatterRbi(game, batterName, runsScored);
                    incrementPitcherEarnedRuns(game, pitcherName, runsScored);
                }
                game.setStrike(0);
                game.setBall(0);
                addPlayEvent(game, "PA_END", hitResult, batterName, pitcherName, "타구 결과: " + hitResult);
                stateService.checkGameOver(gameId);
                if (game.getOut() < 3 && !game.isGameOver()) {
                    stateService.advanceBattingOrder(gameId);
                }
                break;
            }
            case "홈런": {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int runs = 1;
                com.baseball.game.dto.Batter[] bases = game.getBases();
                if (bases != null) {
                    if (bases[1] != null)
                        runs++;
                    if (bases[2] != null)
                        runs++;
                    if (bases[3] != null)
                        runs++;
                }
                if (game.isTop())
                    game.setAwayScore(game.getAwayScore() + runs);
                else
                    game.setHomeScore(game.getHomeScore() + runs);
                // 타점/자책점 누적
                incrementBatterRbi(game, batterName, runs);
                incrementPitcherEarnedRuns(game, pitcherName, runs);
                // 팀 히트 누적 (홈런도 히트 1)
                if (game.isTop()) {
                    game.setAwayHit(game.getAwayHit() + 1);
                } else {
                    game.setHomeHit(game.getHomeHit() + 1);
                }
                GameLogicUtil.resetBases(game);
                game.setStrike(0);
                game.setBall(0);
                addPlayEvent(game, "PA_END", "홈런", batterName, pitcherName, "홈런으로 " + runs + "득점");
                stateService.checkGameOver(gameId);
                if (game.getOut() < 3 && !game.isGameOver()) {
                    stateService.advanceBattingOrder(gameId);
                }
                // 스탯 누적: 타자 PA/AB/H + HR, 투수 피안타/피홈런
                accumulateBatterPaAbHit(game, batterName, true, true, true);
                incrementBatterHomer(game, batterName);
                incrementPitcherHitAllowed(game, pitcherName);
                incrementPitcherHomerAllowed(game, pitcherName);
                break;
            }
            case "뜬공 아웃":
            case "삼진 아웃":
            case "땅볼 아웃":
            case "병살타": {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int beforeOuts = game.getOut();
                int runsBefore = getOffenseScore(game);
                if ("땅볼 아웃".equals(hitResult) || "병살타".equals(hitResult)) {
                    String gbResult = GameLogicUtil.processGroundBall(game, game.getCurrentBatter());
                    hitResult = gbResult;
                }
                if ("삼진 아웃".equals(hitResult) || "뜬공 아웃".equals(hitResult)) {
                    game.setOut(beforeOuts + 1);
                }
                // 득점 후 계산 (강제 득점 반영)
                int runsAfter = getOffenseScore(game);
                int runsScored = Math.max(0, runsAfter - runsBefore);
                int outsGained = game.getOut() - beforeOuts; // 전환 전 계산
                game.setStrike(0);
                game.setBall(0);
                addPlayEvent(game, "PA_END", hitResult, batterName, pitcherName, "아웃: " + hitResult, beforeOuts);
                if (game.getOut() >= 3)
                    stateService.nextInning(gameId);
                else
                    stateService.advanceBattingOrder(gameId);
                // 스탯 누적: 타석 + 타수, 삼진이면 삼진 증가, 투수 아웃 수 증가
                accumulateBatterPaAbHit(game, batterName, true, true, false);
                if ("삼진 아웃".equals(hitResult)) {
                    incrementBatterStrikeout(game, batterName);
                    incrementPitcherStrikeout(game, pitcherName);
                }
                if (outsGained > 0) {
                    incrementPitcherOut(game, pitcherName, outsGained);
                }
                // 강제 득점 발생 시 타점/자책점 누적
                if (runsScored > 0) {
                    incrementBatterRbi(game, batterName, runsScored);
                    incrementPitcherEarnedRuns(game, pitcherName, runsScored);
                }
                break;
            }
            default: {
                String batterName = game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : null;
                String pitcherName = game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : null;
                int beforeOuts = game.getOut();
                game.setOut(beforeOuts + 1);
                int outsGained = game.getOut() - beforeOuts; // 전환 전 계산
                addPlayEvent(game, "PA_END", hitResult, batterName, pitcherName, "타석 종료: " + hitResult, beforeOuts);
                game.setStrike(0);
                game.setBall(0);
                if (game.getOut() >= 3)
                    stateService.nextInning(gameId);
                else
                    stateService.advanceBattingOrder(gameId);
                // 스탯 누적: 일반 아웃 → 타석+타수
                accumulateBatterPaAbHit(game, batterName, true, true, false);
                if (outsGained > 0) {
                    incrementPitcherOut(game, pitcherName, outsGained);
                }
                break;
            }
        }
        return hitResult;
    }

    private void addPlayEvent(GameDto game, String type, String result, String batterName, String pitcherName,
            String description) {
        try {
            com.baseball.game.dto.PlayEvent event = com.baseball.game.dto.PlayEvent.builder()
                    .type(type)
                    .inning(game.getInning())
                    .isTop(game.isTop())
                    .offenseTeam(game.getOffenseTeam())
                    .batter(batterName)
                    .pitcher(pitcherName)
                    .result(result)
                    .description(description)
                    .out(game.getOut())
                    .strike(game.getStrike())
                    .ball(game.getBall())
                    .homeScore(game.getHomeScore())
                    .awayScore(game.getAwayScore())
                    .homeHit(game.getHomeHit())
                    .awayHit(game.getAwayHit())
                    .homeWalks(game.getHomeWalks())
                    .awayWalks(game.getAwayWalks())
                    .build();
            if (game.getEventLog() == null) {
                game.setEventLog(new java.util.ArrayList<>());
            }
            game.getEventLog().add(event);
        } catch (Exception ignored) {
        }
    }

    private void addPlayEvent(GameDto game, String type, String result, String batterName, String pitcherName,
            String description, Integer outsSnapshot) {
        try {
            int outsValue = (outsSnapshot != null) ? outsSnapshot.intValue() : game.getOut();
            com.baseball.game.dto.PlayEvent event = com.baseball.game.dto.PlayEvent.builder()
                    .type(type)
                    .inning(game.getInning())
                    .isTop(game.isTop())
                    .offenseTeam(game.getOffenseTeam())
                    .batter(batterName)
                    .pitcher(pitcherName)
                    .result(result)
                    .description(description)
                    .out(outsValue)
                    .strike(game.getStrike())
                    .ball(game.getBall())
                    .homeScore(game.getHomeScore())
                    .awayScore(game.getAwayScore())
                    .homeHit(game.getHomeHit())
                    .awayHit(game.getAwayHit())
                    .homeWalks(game.getHomeWalks())
                    .awayWalks(game.getAwayWalks())
                    .build();
            if (game.getEventLog() == null) {
                game.setEventLog(new java.util.ArrayList<>());
            }
            game.getEventLog().add(event);
        } catch (Exception ignored) {
        }
    }

    private void accumulateBatterPaAbHit(GameDto game, String batterName, boolean addPA, boolean addAB,
            boolean addHit) {
        if (batterName == null)
            return;
        if (game.getBatterGameStatsMap() == null)
            game.setBatterGameStatsMap(new java.util.HashMap<>());
        com.baseball.game.dto.BatterGameStats s = game.getBatterGameStatsMap().getOrDefault(batterName,
                com.baseball.game.dto.BatterGameStats.builder().playerName(batterName).build());
        if (addPA)
            s.setPlateAppearances(s.getPlateAppearances() + 1);
        if (addAB)
            s.setAtBats(s.getAtBats() + 1);
        if (addHit)
            s.setHits(s.getHits() + 1);
        game.getBatterGameStatsMap().put(batterName, s);
    }

    private void incrementBatterHomer(GameDto game, String batterName) {
        if (batterName == null)
            return;
        com.baseball.game.dto.BatterGameStats s = game.getBatterGameStatsMap().get(batterName);
        if (s != null)
            s.setHomeRuns(s.getHomeRuns() + 1);
    }

    private void incrementBatterStrikeout(GameDto game, String batterName) {
        if (batterName == null)
            return;
        com.baseball.game.dto.BatterGameStats s = game.getBatterGameStatsMap().get(batterName);
        if (s != null)
            s.setStrikeouts(s.getStrikeouts() + 1);
    }

    private void incrementPitcherHitAllowed(GameDto game, String pitcherName) {
        if (pitcherName == null)
            return;
        if (game.getPitcherGameStatsMap() == null)
            game.setPitcherGameStatsMap(new java.util.HashMap<>());
        com.baseball.game.dto.PitcherGameStats s = game.getPitcherGameStatsMap().getOrDefault(pitcherName,
                com.baseball.game.dto.PitcherGameStats.builder().playerName(pitcherName).build());
        s.setHitsAllowed(s.getHitsAllowed() + 1);
        game.getPitcherGameStatsMap().put(pitcherName, s);
    }

    private void incrementPitcherHomerAllowed(GameDto game, String pitcherName) {
        if (pitcherName == null)
            return;
        com.baseball.game.dto.PitcherGameStats s = game.getPitcherGameStatsMap().get(pitcherName);
        if (s != null)
            s.setHomersAllowed(s.getHomersAllowed() + 1);
    }

    private void incrementPitcherStrikeout(GameDto game, String pitcherName) {
        if (pitcherName == null)
            return;
        if (game.getPitcherGameStatsMap() == null)
            game.setPitcherGameStatsMap(new java.util.HashMap<>());
        com.baseball.game.dto.PitcherGameStats s = game.getPitcherGameStatsMap().getOrDefault(pitcherName,
                com.baseball.game.dto.PitcherGameStats.builder().playerName(pitcherName).build());
        s.setStrikeouts(s.getStrikeouts() + 1);
        game.getPitcherGameStatsMap().put(pitcherName, s);
    }

    private void incrementPitcherOut(GameDto game, String pitcherName, int outs) {
        if (pitcherName == null || outs <= 0)
            return;
        if (game.getPitcherGameStatsMap() == null)
            game.setPitcherGameStatsMap(new java.util.HashMap<>());
        com.baseball.game.dto.PitcherGameStats s = game.getPitcherGameStatsMap().getOrDefault(pitcherName,
                com.baseball.game.dto.PitcherGameStats.builder().playerName(pitcherName).build());
        s.setOutsRecorded(s.getOutsRecorded() + outs);
        game.getPitcherGameStatsMap().put(pitcherName, s);
    }

    private void incrementPitchCount(GameDto game, String pitcherName, int pitches) {
        if (pitcherName == null || pitches <= 0)
            return;
        if (game.getPitcherGameStatsMap() == null)
            game.setPitcherGameStatsMap(new java.util.HashMap<>());
        com.baseball.game.dto.PitcherGameStats s = game.getPitcherGameStatsMap().getOrDefault(pitcherName,
                com.baseball.game.dto.PitcherGameStats.builder().playerName(pitcherName).build());
        s.setPitches(s.getPitches() + pitches);
        game.getPitcherGameStatsMap().put(pitcherName, s);
    }

    private int getOffenseScore(GameDto game) {
        return game.isTop() ? game.getAwayScore() : game.getHomeScore();
    }

    private void incrementBatterRbi(GameDto game, String batterName, int runs) {
        if (batterName == null || runs <= 0)
            return;
        if (game.getBatterGameStatsMap() == null)
            game.setBatterGameStatsMap(new java.util.HashMap<>());
        com.baseball.game.dto.BatterGameStats s = game.getBatterGameStatsMap().getOrDefault(batterName,
                com.baseball.game.dto.BatterGameStats.builder().playerName(batterName).build());
        s.setRbis(s.getRbis() + runs);
        game.getBatterGameStatsMap().put(batterName, s);
    }

    private void incrementPitcherEarnedRuns(GameDto game, String pitcherName, int runs) {
        if (pitcherName == null || runs <= 0)
            return;
        if (game.getPitcherGameStatsMap() == null)
            game.setPitcherGameStatsMap(new java.util.HashMap<>());
        com.baseball.game.dto.PitcherGameStats s = game.getPitcherGameStatsMap().getOrDefault(pitcherName,
                com.baseball.game.dto.PitcherGameStats.builder().playerName(pitcherName).build());
        s.setEarnedRunsAllowed(s.getEarnedRunsAllowed() + runs);
        game.getPitcherGameStatsMap().put(pitcherName, s);
    }

    private void incrementTeamHit(GameDto game) {
        if (game == null)
            return;
        if (game.isTop()) {
            game.setAwayHit(game.getAwayHit() + 1);
        } else {
            game.setHomeHit(game.getHomeHit() + 1);
        }
    }
}