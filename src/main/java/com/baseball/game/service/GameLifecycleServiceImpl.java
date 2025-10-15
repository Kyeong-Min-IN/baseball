package com.baseball.game.service;

import com.baseball.game.dto.GameDto;
import com.baseball.game.exception.GameNotFoundException;
import com.baseball.game.exception.ValidationException;
import com.baseball.game.util.GameLogicUtil;
import com.baseball.game.mapper.BatterMapper;
import com.baseball.game.mapper.PitcherMapper;
import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class GameLifecycleServiceImpl implements GameLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(GameLifecycleServiceImpl.class);

    // 게임 데이터를 메모리에 저장하는 Map (동시성 안전)
    private final Map<String, GameDto> games = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired
    private BatterMapper batterMapper;

    @org.springframework.beans.factory.annotation.Autowired
    private PitcherMapper pitcherMapper;

    @Override
    @Transactional
    public GameDto createGame(String homeTeam, String awayTeam, int maxInning, boolean isUserOffense) {
        // 팀/입력값 검증
        if (homeTeam == null || awayTeam == null || homeTeam.trim().isEmpty() || awayTeam.trim().isEmpty()) {
            throw new ValidationException("홈팀과 원정팀 이름은 필수입니다.");
        }
        if (homeTeam.equals(awayTeam)) {
            throw new ValidationException("홈팀과 원정팀은 동일할 수 없습니다.");
        }
        if (maxInning <= 0) {
            throw new ValidationException("최대 이닝 수는 1 이상이어야 합니다.");
        }

        // 별칭/축약/영문 등을 표준 표기(ComputerLineupProvider 키)로 정규화
        final String normalizedHome = normalizeToDisplayName(homeTeam);
        final String normalizedAway = normalizeToDisplayName(awayTeam);

        GameDto newGame = new GameDto();
        newGame.setGameId(UUID.randomUUID().toString());
        newGame.setHomeTeam(normalizedHome);
        newGame.setAwayTeam(normalizedAway);
        newGame.setMaxInning(maxInning);
        newGame.setUserOffense(isUserOffense);
        // userId는 컨트롤러 요청에서 내려온 정보를 서비스 계층에서 세팅하도록 확장 (별도 오버로드 고려)
        newGame.setInning(1);
        newGame.setTop(true);
        newGame.setOut(0);
        newGame.setStrike(0);
        newGame.setBall(0);
        newGame.setHomeScore(0);
        newGame.setAwayScore(0);
        GameLogicUtil.resetBases(newGame);
        newGame.setGameOver(false);
        newGame.setWinner(null);
        // 초기 공격/수비 팀 설정 (1회 초: 원정 공격, 홈 수비)
        newGame.setAwayTeam(newGame.isTop() ? normalizedAway : normalizedHome);
        newGame.setHomeTeam(newGame.isTop() ? normalizedHome : normalizedAway);

        // 인메모리 기본 라인업 사용 (정규화된 팀명으로 조회)
        applyInMemoryDefaultLineups(newGame, normalizedHome, normalizedAway);

        games.put(newGame.getGameId(), newGame);
        log.info("Created game with ID: {}", newGame.getGameId());

        return newGame;
    }

    @Override
    public GameDto getGame(String gameId) {
        GameDto game = games.get(gameId);
        if (game == null) {
            throw new GameNotFoundException("게임을 찾을 수 없습니다: " + gameId);
        }
        return game;
    }

    @Override
    @Transactional
    public void resetGame(String gameId) {
        GameDto game = getGame(gameId);

        // 게임 상태 초기화
        game.setInning(1);
        game.setTop(true);
        game.setOut(0);
        game.setStrike(0);
        game.setBall(0);
        game.setHomeScore(0);
        game.setAwayScore(0);
        GameLogicUtil.resetBases(game);
        game.setGameOver(false);
        game.setWinner(null);
        // 라인업/선발투수도 기본값으로 재설정 (게임 생성 당시 팀 기준)
        game.setCurrentBatterIndex(0);
        game.setHomeBatterIndex(0);
        game.setAwayBatterIndex(0);
        applyInMemoryDefaultLineups(game, game.getHomeTeam(), game.getAwayTeam());

        log.info("Game reset: {}", gameId);
    }

    @Override
    public boolean existsGame(String gameId) {
        return games.containsKey(gameId);
    }

    /**
     * 내부적으로 게임을 저장하는 메서드 (다른 서비스에서 사용)
     */
    protected void saveGame(GameDto game) {
        games.put(game.getGameId(), game);
    }

    @Override
    public void saveGame(String gameId) {
        // 인메모리 구현에서는 별도 작업 불필요. 저장은 games 맵에 이미 반영됨.
    }

    private List<Batter> orderBattersByNames(List<String> names) {
        if (names == null || names.isEmpty())
            return new ArrayList<>();
        List<Batter> fetched = (batterMapper != null) ? batterMapper.findByNames(names) : null;
        Map<String, Batter> byName = new java.util.HashMap<>();
        if (fetched != null) {
            for (Batter b : fetched) {
                if (b != null && b.getName() != null) {
                    // 타율이 비어 있으면 계산값 반영
                    if (b.getAtBats() > 0 && (b.getBattingAverage() == 0.0)) {
                        b.setBattingAverage(com.baseball.game.util.GameStatisticsUtil.calculateBattingAverage(b));
                    }
                    byName.put(b.getName(), b);
                }
            }
        }
        List<Batter> ordered = new ArrayList<>();
        for (String n : names) {
            Batter b = byName.get(n);
            if (b != null) {
                ordered.add(b);
            }
        }
        return ordered;
    }

    private void applyInMemoryDefaultLineups(GameDto game, String homeTeam, String awayTeam) {
        try {
            List<String> homeNames = ComputerLineupProvider.getDefaultBattingOrder(homeTeam);
            List<String> awayNames = ComputerLineupProvider.getDefaultBattingOrder(awayTeam);

            // 매퍼가 있더라도 조회 실패 시 이름 기반으로 폴백
            List<Batter> homeBatters;
            List<Batter> awayBatters;
            if (batterMapper != null) {
                try {
                    homeBatters = orderBattersByNames(homeNames);
                } catch (Throwable t) {
                    log.warn("타자 라인업 DB 조회 실패(홈). 이름 기반 폴백을 사용합니다.", t);
                    homeBatters = null;
                }
                if (homeBatters == null || homeBatters.isEmpty()) {
                    homeBatters = new ArrayList<>();
                    for (String n : homeNames)
                        homeBatters.add(new com.baseball.game.dto.Batter(n, homeTeam));
                }
                try {
                    awayBatters = orderBattersByNames(awayNames);
                } catch (Throwable t) {
                    log.warn("타자 라인업 DB 조회 실패(원정). 이름 기반 폴백을 사용합니다.", t);
                    awayBatters = null;
                }
                if (awayBatters == null || awayBatters.isEmpty()) {
                    awayBatters = new ArrayList<>();
                    for (String n : awayNames)
                        awayBatters.add(new com.baseball.game.dto.Batter(n, awayTeam));
                }
            } else {
                homeBatters = new ArrayList<>();
                for (String n : homeNames)
                    homeBatters.add(new com.baseball.game.dto.Batter(n, homeTeam));
                awayBatters = new ArrayList<>();
                for (String n : awayNames)
                    awayBatters.add(new com.baseball.game.dto.Batter(n, awayTeam));
            }

            String homeSPName = ComputerLineupProvider.getDefaultStartingPitcher(homeTeam);
            String awaySPName = ComputerLineupProvider.getDefaultStartingPitcher(awayTeam);

            Pitcher homeSP = null;
            Pitcher awaySP = null;
            if (pitcherMapper != null) {
                try {
                    homeSP = homeSPName != null ? pitcherMapper.findByName(homeSPName) : null;
                } catch (Throwable t) {
                    // log.warn("선발투수 DB 조회 실패(홈). 이름 기반 폴백을 사용합니다.", t);
                    homeSP = null;
                }
                try {
                    awaySP = awaySPName != null ? pitcherMapper.findByName(awaySPName) : null;
                } catch (Throwable t) {
                    // log.warn("선발투수 DB 조회 실패(원정). 이름 기반 폴백을 사용합니다.", t);
                    awaySP = null;
                }
            }
            if (homeSP == null && homeSPName != null)
                homeSP = new com.baseball.game.dto.Pitcher(homeSPName, homeTeam);
            if (awaySP == null && awaySPName != null)
                awaySP = new com.baseball.game.dto.Pitcher(awaySPName, awayTeam);

            game.setHomeBattingOrder(homeBatters);
            game.setAwayBattingOrder(awayBatters);
            game.setHomeStartingPitcher(homeSP);
            game.setAwayStartingPitcher(awaySP);

            game.setCurrentBatterIndex(0);
            if (game.isTop()) {
                if (!game.getAwayBattingOrder().isEmpty()) {
                    game.setCurrentBatter(game.getAwayBattingOrder().get(0));
                }
                game.setCurrentPitcher(game.getHomeStartingPitcher());
            } else {
                if (!game.getHomeBattingOrder().isEmpty()) {
                    game.setCurrentBatter(game.getHomeBattingOrder().get(0));
                }
                game.setCurrentPitcher(game.getAwayStartingPitcher());
            }
        } catch (Throwable t) {
            log.warn("인메모리 기본 라인업 적용 실패. 빈 라인업으로 생성됩니다.", t);
            game.setCurrentBatter(null);
            game.setCurrentPitcher(null);
            game.setHomeStartingPitcher(null);
            game.setAwayStartingPitcher(null);
            game.setHomeBattingOrder(new ArrayList<>());
            game.setAwayBattingOrder(new ArrayList<>());
            game.setCurrentBatterIndex(0);
        }
    }

    // ---------------- 팀명 정규화 (별칭/축약/영문 → 표준 표기) ----------------
    private static final java.util.Map<String, String> DISPLAY_NAME_ALIASES = new java.util.HashMap<>();
    static {
        // 두산
        DISPLAY_NAME_ALIASES.put("두산", "두산 베어스");
        DISPLAY_NAME_ALIASES.put("bears", "두산 베어스");
        DISPLAY_NAME_ALIASES.put("두산 베어스", "두산 베어스");
        // LG
        DISPLAY_NAME_ALIASES.put("lg", "LG 트윈스");
        DISPLAY_NAME_ALIASES.put("twins", "LG 트윈스");
        DISPLAY_NAME_ALIASES.put("lg 트윈스", "LG 트윈스");

        // SSG
        DISPLAY_NAME_ALIASES.put("ssg", "SSG 랜더스");
        DISPLAY_NAME_ALIASES.put("landers", "SSG 랜더스");
        DISPLAY_NAME_ALIASES.put("ssg 랜더스", "SSG 랜더스");
        // 키움
        DISPLAY_NAME_ALIASES.put("키움", "키움 히어로즈");
        DISPLAY_NAME_ALIASES.put("heros", "키움 히어로즈");
        DISPLAY_NAME_ALIASES.put("키움 히어로즈", "키움 히어로즈");
        // 한화
        DISPLAY_NAME_ALIASES.put("한화", "한화 이글스");
        DISPLAY_NAME_ALIASES.put("eagles", "한화 이글스");
        DISPLAY_NAME_ALIASES.put("한화 이글스", "한화 이글스");
        // 롯데
        DISPLAY_NAME_ALIASES.put("롯데", "롯데 자이언츠");
        DISPLAY_NAME_ALIASES.put("giants", "롯데 자이언츠");
        DISPLAY_NAME_ALIASES.put("롯데 자이언츠", "롯데 자이언츠");
        // 삼성
        DISPLAY_NAME_ALIASES.put("삼성", "삼성 라이온즈");
        DISPLAY_NAME_ALIASES.put("lions", "삼성 라이온즈");
        DISPLAY_NAME_ALIASES.put("삼성 라이온즈", "삼성 라이온즈");
        // KT
        DISPLAY_NAME_ALIASES.put("kt", "kt 위즈");
        DISPLAY_NAME_ALIASES.put("kt 위즈", "kt 위즈");
        DISPLAY_NAME_ALIASES.put("wiz", "kt 위즈");
        // KIA
        DISPLAY_NAME_ALIASES.put("kia", "KIA 타이거즈");
        DISPLAY_NAME_ALIASES.put("타이거즈", "KIA 타이거즈");
        // NC
        DISPLAY_NAME_ALIASES.put("nc", "NC 다이노스");
        DISPLAY_NAME_ALIASES.put("dinos", "NC 다이노스");
        DISPLAY_NAME_ALIASES.put("NC 다이노스", "NC 다이노스");
    }

    private String normalizeToDisplayName(String teamName) {
        if (teamName == null)
            return null;
        String key = teamName.trim();
        String lower = key.toLowerCase();
        String mapped = DISPLAY_NAME_ALIASES.get(lower);
        if (mapped != null)
            return mapped;
        // 이미 정확 표기면 그대로 사용
        if (DISPLAY_NAME_ALIASES.containsValue(key))
            return key;
        return key; // 그대로 사용 (추가 매핑 누락 케이스 대비)
    }
}