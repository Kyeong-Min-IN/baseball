package com.baseball.game.service;

import com.baseball.game.dto.GameDto;
import com.baseball.game.mapper.BatterMapper;
import com.baseball.game.mapper.PitcherMapper;
import com.baseball.game.dto.TeamLineupSetRequest;
import com.baseball.game.util.ValidationUtil;
import com.baseball.game.service.GameService;
import com.baseball.game.service.GameLifecycleService;
import com.baseball.game.service.GameStateService;
import com.baseball.game.service.GameActionService;
import com.baseball.game.service.GameValidationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Setter;

@Service
@Transactional
public class GameServiceImpl implements GameService {
    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);

    @Autowired
    private GameLifecycleService lifecycleService;

    @Autowired
    private GameStateService stateService;

    @Autowired
    private GameActionService actionService;

    @Autowired
    private GameValidationService validationService;

    @Setter(onMethod_ = @Autowired)
    private BatterMapper batterMapper;

    @Setter(onMethod_ = @Autowired)
    private PitcherMapper pitcherMapper;

    @Autowired
    private TeamLineupService teamLineupService;

    // 요청 팀명을 DB 팀 표기로 정규화하기 위한 매핑 (부분 중복 허용)
    private static final java.util.Map<String, String> DB_TEAM_NAMES = new java.util.HashMap<>();
    static {
        DB_TEAM_NAMES.put("Landers", "SSG");
        DB_TEAM_NAMES.put("SSG 랜더스", "SSG");
        DB_TEAM_NAMES.put("SSG", "SSG");

        DB_TEAM_NAMES.put("Bears", "두산");
        DB_TEAM_NAMES.put("두산 베어스", "두산");
        DB_TEAM_NAMES.put("두산", "두산");

        DB_TEAM_NAMES.put("Lions", "삼성");
        DB_TEAM_NAMES.put("삼성 라이온즈", "삼성");
        DB_TEAM_NAMES.put("삼성", "삼성");

        DB_TEAM_NAMES.put("Twins", "LG");
        DB_TEAM_NAMES.put("LG 트윈스", "LG");
        DB_TEAM_NAMES.put("LG", "LG");

        DB_TEAM_NAMES.put("Wiz", "KT");
        DB_TEAM_NAMES.put("kt 위즈", "KT");
        DB_TEAM_NAMES.put("KT", "KT");
        DB_TEAM_NAMES.put("kt", "KT");

        DB_TEAM_NAMES.put("Giants", "롯데");
        DB_TEAM_NAMES.put("롯데 자이언츠", "롯데");
        DB_TEAM_NAMES.put("롯데", "롯데");

        DB_TEAM_NAMES.put("Eagles", "한화");
        DB_TEAM_NAMES.put("한화 이글스", "한화");
        DB_TEAM_NAMES.put("한화", "한화");

        DB_TEAM_NAMES.put("Dinos", "NC");
        DB_TEAM_NAMES.put("NC 다이노스", "NC");
        DB_TEAM_NAMES.put("NC", "NC");

        DB_TEAM_NAMES.put("Heros", "키움");
        DB_TEAM_NAMES.put("키움 히어로즈", "키움");
        DB_TEAM_NAMES.put("키움", "키움");

        DB_TEAM_NAMES.put("Tigers", "KIA");
        DB_TEAM_NAMES.put("KIA 타이거즈", "KIA");
        DB_TEAM_NAMES.put("KIA", "KIA");
    }

    private static String getOrDefaultIgnoreCase(java.util.Map<String, String> map, String key, String defaultValue) {
        if (key == null)
            return defaultValue;
        for (java.util.Map.Entry<String, String> e : map.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return defaultValue;
    }

    // 게임 데이터는 GameLifecycleServiceImpl에서 관리됩니다.

    @Override
    @Transactional
    public GameDto createGame(String homeTeam, String awayTeam, int maxInning, boolean isUserOffense) {
        return lifecycleService.createGame(homeTeam, awayTeam, maxInning, isUserOffense);
    }

    @Override
    public GameDto getGame(String gameId) {
        return lifecycleService.getGame(gameId);
    }

    @Override
    @Transactional
    public String batterSwing(String gameId, Boolean swing, Boolean timing) {
        return actionService.batterSwing(gameId, swing, timing);
    }

    // GameServiceImpl.java 파일의 pitcherThrow 메서드를 아래와 같이 수정합니다.

    @Override
    @Transactional
    public String pitcherThrow(String gameId, String pitchType) {
        return actionService.pitcherThrow(gameId, pitchType);
    }
    // GameServiceImpl에 추가할 AI 제어 메서드 (예시)

    @Override
    public String playComputerTurn(String gameId) {
        return actionService.playComputerTurn(gameId);
    }

    // 이 메서드 외에 다음 메서드는 GameLogicUtil.java에 이미 존재합니다.
    // private static int calculateContactFromBattingAverage(double battingAverage)
    // { ... }

    @Override
    @Transactional
    public GameDto nextInning(String gameId) {
        return stateService.nextInning(gameId);
    }

    @Override
    @Transactional
    public GameDto endGame(String gameId) {
        return stateService.endGame(gameId);
    }

    @Override
    @Transactional
    public void advanceRunners(String gameId, Integer basesToAdvance) {
        stateService.advanceRunners(gameId, basesToAdvance);
    }

    // 위임 패턴 적용으로 인해 더 이상 필요하지 않은 메서드들
    // 이 메서드들은 각각의 세분화된 서비스로 이동되었습니다.

    @Override
    public void resetGame(String gameId) {
        lifecycleService.resetGame(gameId);
    }

    @Override
    public GameDto applyTeamLineup(String gameId, TeamLineupSetRequest request) {
        ValidationUtil.validateGameId(gameId);
        ValidationUtil.validateTeamLineupSetRequest(request);

        GameDto game = lifecycleService.getGame(gameId);
        // 팀명 정규화 후 홈/원정 매칭 판단
        String reqDbTeam = getOrDefaultIgnoreCase(DB_TEAM_NAMES, request.getTeamName(), request.getTeamName());
        String homeDbTeam = getOrDefaultIgnoreCase(DB_TEAM_NAMES, game.getHomeTeam(), game.getHomeTeam());
        String awayDbTeam = getOrDefaultIgnoreCase(DB_TEAM_NAMES, game.getAwayTeam(), game.getAwayTeam());
        boolean isHome = reqDbTeam.equalsIgnoreCase(homeDbTeam);
        boolean isAway = reqDbTeam.equalsIgnoreCase(awayDbTeam);
        if (!isHome && !isAway) {
            throw new com.baseball.game.exception.ValidationException("요청 팀이 게임의 홈/원정팀과 일치하지 않습니다.");
        }

        // 사용자 팀만 라인업 변경 가능: isUserOffense=true면 사용자 팀은 원정팀, false면 홈팀
        // boolean isUsersTeam = (game.isUserOffense() && isAway) || (!game.isUserOffense() && isHome);
        // if (!isUsersTeam) {
        //     throw new com.baseball.game.exception.ValidationException("사용자 팀이 아닌 팀의 라인업은 변경할 수 없습니다.");
        // }

        // 요청 팀을 DB 팀 표기로 정규화
        String dbTeamOfRequest = reqDbTeam;

        // 타자 9명 로딩 및 선발 투수 로딩
        java.util.List<com.baseball.game.dto.Batter> batters = batterMapper.findByNames(request.getBattingOrder());
        java.util.Map<String, com.baseball.game.dto.Batter> byName = new java.util.HashMap<>();
        if (batters != null) {
            for (com.baseball.game.dto.Batter b : batters) {
                if (b != null && b.getName() != null) {
                    if (b.getAtBats() > 0 && b.getBattingAverage() == 0.0) {
                        b.setBattingAverage(b.calculateBattingAverage());
                    }
                    byName.put(b.getName(), b);
                }
            }
        }
        java.util.List<com.baseball.game.dto.Batter> ordered = new java.util.ArrayList<>();
        for (String n : request.getBattingOrder()) {
            com.baseball.game.dto.Batter b = byName.get(n);
            if (b == null) {
                throw new com.baseball.game.exception.ValidationException("타자를 찾을 수 없습니다: " + n);
            }
            String bTeam = b.getTeam() == null ? null : b.getTeam().trim();
            String reqTeam = dbTeamOfRequest == null ? null : dbTeamOfRequest.trim();
            if (bTeam == null || reqTeam == null || !bTeam.equalsIgnoreCase(reqTeam)) {
                throw new com.baseball.game.exception.ValidationException(
                        "요청 팀과 다른 팀 소속의 타자입니다: " + n + " (소속: " + b.getTeam() + ")");
            }
            ordered.add(b);
        }

        com.baseball.game.dto.Pitcher sp = pitcherMapper.findByName(request.getStartingPitcher());
        if (sp == null) {
            throw new com.baseball.game.exception.ValidationException(
                    "선발 투수를 찾을 수 없습니다: " + request.getStartingPitcher());
        }
        String spTeam = sp.getTeam() == null ? null : sp.getTeam().trim();
        String reqTeam2 = dbTeamOfRequest == null ? null : dbTeamOfRequest.trim();
        if (spTeam == null || reqTeam2 == null || !spTeam.equalsIgnoreCase(reqTeam2)) {
            throw new com.baseball.game.exception.ValidationException(
                    "요청 팀과 다른 팀 소속의 선발 투수입니다: " + sp.getName() + " (소속: " + sp.getTeam() + ")");
        }

        if (isHome) {
            game.setHomeBattingOrder(ordered);
            game.setHomeStartingPitcher(sp);
            if (!game.isTop()) {
                // 말(홈 공격)일 때: 홈 타자 시작, 수비 투수는 원정 선발
                game.setCurrentBatterIndex(0);
                if (!ordered.isEmpty())
                    game.setCurrentBatter(ordered.get(0));
                game.setCurrentPitcher(game.getAwayStartingPitcher());
            } else {
                // 초(원정 공격)일 때: 수비는 홈팀이므로 현재 투수를 홈 선발로 반영
                game.setCurrentPitcher(game.getHomeStartingPitcher());
            }
        } else {
            game.setAwayBattingOrder(ordered);
            game.setAwayStartingPitcher(sp);
            if (game.isTop()) {
                // 초(원정 공격)일 때: 원정 타자 시작, 수비 투수는 홈 선발
                game.setCurrentBatterIndex(0);
                if (!ordered.isEmpty())
                    game.setCurrentBatter(ordered.get(0));
                game.setCurrentPitcher(game.getHomeStartingPitcher());
            } else {
                // 말(홈 공격)일 때: 수비는 원정이므로 현재 투수를 원정 선발로 반영
                game.setCurrentPitcher(game.getAwayStartingPitcher());
            }
        }

        return game;
    }
}