package com.baseball.game.service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baseball.game.dto.TeamLineup;
import com.baseball.game.dto.CustomLineupRequest;
import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;
import com.baseball.game.mapper.TeamLineupMapper;
import com.baseball.game.mapper.BatterMapper;
import com.baseball.game.mapper.PitcherMapper;
import com.baseball.game.exception.ValidationException;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TeamLineupServiceImpl implements TeamLineupService {

    private static final Logger logger = LoggerFactory.getLogger(TeamLineupServiceImpl.class);

    @Setter(onMethod_ = @Autowired)
    private TeamLineupMapper teamLineupMapper;

    @Setter(onMethod_ = @Autowired)
    private BatterMapper batterMapper;

    @Setter(onMethod_ = @Autowired)
    private PitcherMapper pitcherMapper;

    private final Map<String, Map<String, List<Batter>>> userCustomBattingOrders = new java.util.HashMap<>();
    private final Map<String, Map<String, Pitcher>> userCustomStartingPitchers = new java.util.HashMap<>();

    // 팀명 별칭 매핑 (한국어 → 내부 코드)
    private static final Map<String, String> TEAM_ALIASES = new java.util.HashMap<>();
    // DB 팀명 매핑 (내부/별칭 → DB 저장값)
    private static final Map<String, String> DB_TEAM_NAMES = new java.util.HashMap<>();
    // 컴퓨터 기본 라인업 제공자가 기대하는 한글 풀네임 매핑 (내부 코드/별칭 → 풀네임)
    private static final Map<String, String> DISPLAY_TEAM_NAMES = new java.util.HashMap<>();
    // 응답 표기를 위한 영어 풀네임 매핑 (내부 코드 → 영어 표기)
    private static final Map<String, String> ENGLISH_DISPLAY_NAMES = new java.util.HashMap<>();

    public TeamLineupServiceImpl() {
        // 팀명 별칭 초기화
        TEAM_ALIASES.put("KIA 타이거즈", "Tigers");
        TEAM_ALIASES.put("삼성 라이온즈", "Lions");
        TEAM_ALIASES.put("LG 트윈스", "Twins");
        TEAM_ALIASES.put("두산 베어스", "Bears");
        TEAM_ALIASES.put("kt 위즈", "Wiz");
        TEAM_ALIASES.put("SSG 랜더스", "Landers");
        TEAM_ALIASES.put("롯데 자이언츠", "Giants");
        TEAM_ALIASES.put("한화 이글스", "Eagles");
        TEAM_ALIASES.put("NC 다이노스", "Dinos");
        TEAM_ALIASES.put("키움 히어로즈", "Heros");

        // 축약/별칭도 내부 코드로 인식되게 추가
        TEAM_ALIASES.put("SSG", "Landers");
        TEAM_ALIASES.put("삼성", "Lions");
        TEAM_ALIASES.put("LG", "Twins");
        TEAM_ALIASES.put("엘지", "Twins");
        TEAM_ALIASES.put("두산", "Bears");
        TEAM_ALIASES.put("KT", "Wiz");
        TEAM_ALIASES.put("kt", "Wiz");
        TEAM_ALIASES.put("롯데", "Giants");
        TEAM_ALIASES.put("한화", "Eagles");
        TEAM_ALIASES.put("NC", "Dinos");
        TEAM_ALIASES.put("키움", "Heros");
        TEAM_ALIASES.put("KIA", "Tigers");
        TEAM_ALIASES.put("기아", "Tigers");

        // 영어 약어/별칭도 인식
        TEAM_ALIASES.put("SSG", "Landers");
        TEAM_ALIASES.put("Landers", "Landers");
        TEAM_ALIASES.put("Doosan", "Bears");
        TEAM_ALIASES.put("Bears", "Bears");
        TEAM_ALIASES.put("Samsung", "Lions");
        TEAM_ALIASES.put("Lions", "Lions");
        TEAM_ALIASES.put("LG", "Twins");
        TEAM_ALIASES.put("Twins", "Twins");
        TEAM_ALIASES.put("KT", "Wiz");
        TEAM_ALIASES.put("Wiz", "Wiz");
        TEAM_ALIASES.put("Lotte", "Giants");
        TEAM_ALIASES.put("Giants", "Giants");
        TEAM_ALIASES.put("Hanwha", "Eagles"); // 내부 코드는 기존 철자 유지
        TEAM_ALIASES.put("Eagles", "Eagles");
        TEAM_ALIASES.put("NC", "Dinos");
        TEAM_ALIASES.put("Dinos", "Dinos");
        TEAM_ALIASES.put("Kiwoom", "Heros");
        TEAM_ALIASES.put("Heroes", "Heros");
        TEAM_ALIASES.put("KIA", "Tigers");
        TEAM_ALIASES.put("Tigers", "Tigers");

        // 내부 코드/풀네임/축약 → DB 저장값 매핑
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
        DB_TEAM_NAMES.put("엘지", "LG");

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
        DB_TEAM_NAMES.put("엔씨", "NC");
        
        DB_TEAM_NAMES.put("Heros", "키움");
        DB_TEAM_NAMES.put("키움 히어로즈", "키움");
        DB_TEAM_NAMES.put("키움", "키움");

        DB_TEAM_NAMES.put("Tigers", "KIA");
        DB_TEAM_NAMES.put("KIA 타이거즈", "KIA");
        DB_TEAM_NAMES.put("KIA", "KIA");
        DB_TEAM_NAMES.put("기아", "KIA");

        // 내부 코드/별칭 → 한글 풀네임 (ComputerLineupProvider 키)
        DISPLAY_TEAM_NAMES.put("Landers", "SSG 랜더스");
        DISPLAY_TEAM_NAMES.put("SSG", "SSG 랜더스");
        DISPLAY_TEAM_NAMES.put("SSG 랜더스", "SSG 랜더스");

        DISPLAY_TEAM_NAMES.put("Bears", "두산 베어스");
        DISPLAY_TEAM_NAMES.put("두산", "두산 베어스");
        DISPLAY_TEAM_NAMES.put("두산 베어스", "두산 베어스");

        DISPLAY_TEAM_NAMES.put("Lions", "삼성 라이온즈");
        DISPLAY_TEAM_NAMES.put("삼성", "삼성 라이온즈");
        DISPLAY_TEAM_NAMES.put("삼성 라이온즈", "삼성 라이온즈");

        DISPLAY_TEAM_NAMES.put("Twins", "LG 트윈스");
        DISPLAY_TEAM_NAMES.put("LG", "LG 트윈스");
        DISPLAY_TEAM_NAMES.put("LG 트윈스", "LG 트윈스");
        DISPLAY_TEAM_NAMES.put("엘지", "LG 트윈스");

        DISPLAY_TEAM_NAMES.put("Wiz", "kt 위즈");
        DISPLAY_TEAM_NAMES.put("KT", "kt 위즈");
        DISPLAY_TEAM_NAMES.put("kt", "kt 위즈");
        DISPLAY_TEAM_NAMES.put("kt 위즈", "kt 위즈");
        DISPLAY_TEAM_NAMES.put("케이티", "kt 위즈");

        DISPLAY_TEAM_NAMES.put("Giants", "롯데 자이언츠");
        DISPLAY_TEAM_NAMES.put("롯데", "롯데 자이언츠");
        DISPLAY_TEAM_NAMES.put("롯데 자이언츠", "롯데 자이언츠");

        DISPLAY_TEAM_NAMES.put("Eagles", "한화 이글스");
        DISPLAY_TEAM_NAMES.put("한화", "한화 이글스");
        DISPLAY_TEAM_NAMES.put("한화 이글스", "한화 이글스");

        DISPLAY_TEAM_NAMES.put("Dinos", "NC 다이노스");
        DISPLAY_TEAM_NAMES.put("NC", "NC 다이노스");
        DISPLAY_TEAM_NAMES.put("NC 다이노스", "NC 다이노스");
        DISPLAY_TEAM_NAMES.put("엔씨", "NC 다이노스");

        DISPLAY_TEAM_NAMES.put("Heros", "키움 히어로즈");
        DISPLAY_TEAM_NAMES.put("키움", "키움 히어로즈");
        DISPLAY_TEAM_NAMES.put("키움 히어로즈", "키움 히어로즈");

        DISPLAY_TEAM_NAMES.put("Tigers", "KIA 타이거즈");
        DISPLAY_TEAM_NAMES.put("KIA", "KIA 타이거즈");
        DISPLAY_TEAM_NAMES.put("KIA 타이거즈", "KIA 타이거즈");
        DISPLAY_TEAM_NAMES.put("기아", "KIA 타이거즈");

        // 영어 응답 표기 매핑 (내부 코드 → 영어 풀네임)
        ENGLISH_DISPLAY_NAMES.put("Landers", "SSG Landers");
        ENGLISH_DISPLAY_NAMES.put("Bears", "Doosan Bears");
        ENGLISH_DISPLAY_NAMES.put("Lions", "Samsung Lions");
        ENGLISH_DISPLAY_NAMES.put("Twins", "LG Twins");
        ENGLISH_DISPLAY_NAMES.put("Wiz", "KT Wiz");
        ENGLISH_DISPLAY_NAMES.put("Giants", "Lotte Giants");
        ENGLISH_DISPLAY_NAMES.put("Eagles", "Hanwha Eagles"); // 표기는 올바른 Eagles 사용
        ENGLISH_DISPLAY_NAMES.put("Dinos", "NC Dinos");
        ENGLISH_DISPLAY_NAMES.put("Heros", "Kiwoom Heroes"); // 표기는 Heroes 사용
        ENGLISH_DISPLAY_NAMES.put("Tigers", "KIA Tigers");
    }

    @Override
    public List<TeamLineup> getDefaultLineup(String teamName) {
        // 인메모리 기본 라인업 제공: ComputerLineupProvider 기반으로 TeamLineup 형태 생성
        // 팀명을 정규화(내부 코드)하고, 제공자 호출용 한글 풀네임으로 변환
        String normalizedCode = normalizeTeamName(teamName);
        String displayTeam = resolveDisplayTeamName(normalizedCode);
        List<String> names = ComputerLineupProvider.getDefaultBattingOrder(displayTeam);
        if (names == null || names.size() != 9) {
            throw new ValidationException("기본 라인업은 9명의 타자로 구성되어야 합니다.");
        }
        String englishTeam = resolveEnglishTeamName(normalizedCode);
        List<TeamLineup> lineup = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            TeamLineup tl = new TeamLineup();
            tl.setTeamName(englishTeam);
            tl.setPosition((i + 1) + "th_Batter");
            tl.setPlayerName(names.get(i));
            lineup.add(tl);
        }
        String sp = ComputerLineupProvider.getDefaultStartingPitcher(displayTeam);
        if (sp == null || sp.isEmpty()) {
            throw new ValidationException("선발 투수가 지정되어야 합니다.");
        }
        TeamLineup tlPitcher = new TeamLineup();
        tlPitcher.setTeamName(englishTeam);
        tlPitcher.setPosition("Starting_Pitcher");
        tlPitcher.setPlayerName(sp);
        lineup.add(tlPitcher);
        logger.info("팀 {}의 기본 라인업(인메모리)이 로드되었습니다.", teamName);
        return lineup;
    }

    @Override
    public List<TeamLineup> getCustomLineup(String userId, String teamName) {
        List<TeamLineup> lineup = new ArrayList<>();

        // 사용자 정의 타자 라인업 조회 (메모리)
        List<Batter> customBattingOrder = userCustomBattingOrders
                .getOrDefault(userId, new java.util.HashMap<>())
                .get(teamName);

        if (customBattingOrder != null) {
            for (int i = 0; i < customBattingOrder.size(); i++) {
                Batter batter = customBattingOrder.get(i);
                TeamLineup tl = new TeamLineup();
                tl.setTeamName(teamName);
                tl.setUserId(userId);
                // 타순은 1부터 시작
                tl.setPosition((i + 1) + "th_Batter"); // "1st_Batter", "2nd_Batter" 형식으로 저장
                tl.setPlayerName(batter.getName());
                lineup.add(tl);
            }
        }

        // 사용자 정의 선발 투수 조회 (메모리)
        Pitcher customStartingPitcher = userCustomStartingPitchers
                .getOrDefault(userId, new java.util.HashMap<>())
                .get(teamName);

        if (customStartingPitcher != null) {
            TeamLineup tlPitcher = new TeamLineup();
            tlPitcher.setTeamName(teamName);
            tlPitcher.setUserId(userId);
            tlPitcher.setPosition("Starting_Pitcher"); // 투수는 "Starting_Pitcher"로 저장
            tlPitcher.setPlayerName(customStartingPitcher.getName());
            lineup.add(tlPitcher);
        }

        if (lineup.isEmpty()) {
            logger.warn("사용자 {}의 팀 {}에 대한 커스텀 라인업을 메모리에서 찾을 수 없습니다.", userId, teamName);
        } else {
            logger.info("사용자 {}의 팀 {}에 대한 커스텀 라인업(메모리에서 조회)이 로드되었습니다.", userId, teamName);
        }
        return lineup;
    }

    @Override
    @Transactional
    public void saveCustomLineup(CustomLineupRequest request) {
        userCustomBattingOrders.computeIfAbsent(request.getUserId(), k -> new java.util.HashMap<>())
                .remove(request.getTeamName());
        userCustomStartingPitchers.computeIfAbsent(request.getUserId(), k -> new java.util.HashMap<>())
                .remove(request.getTeamName());

        // 임시 리스트에 타순과 함께 타자를 저장하여 나중에 정렬
        List<Map.Entry<Integer, Batter>> orderedBattersTemp = new ArrayList<>();
        Pitcher newStartingPitcher = null;

        for (CustomLineupRequest.LineupPosition requestPosition : request.getLineup()) {
            String playerName = requestPosition.getPlayerName();
            Integer lineupOrder = requestPosition.getPosition(); // getPosition()이 Integer를 반환한다고 가정
            Integer playerNo = requestPosition.getPlayerNo();

            Batter batter = null;
            Pitcher pitcher = null;
            if (batterMapper != null && playerNo != null) {
                try {
                    batter = batterMapper.findByNo(playerNo);
                } catch (Exception ignored) {
                }
            }
            if (pitcherMapper != null && playerNo != null) {
                try {
                    pitcher = pitcherMapper.findByNo(playerNo);
                } catch (Exception ignored) {
                }
            }

            if (batter != null && batter.getAtBats() > 0 && batter.getBattingAverage() == 0.0) {
                // DB에 타율 컬럼이 없을 경우 계산값 세팅
                batter.setBattingAverage(batter.calculateBattingAverage());
            }

            String normalizedTeam = normalizeTeamName(request.getTeamName());
            if (batter != null && normalizeTeamName(batter.getTeam()).equalsIgnoreCase(normalizedTeam)) {
                // 타자인 경우
                if (lineupOrder == null || lineupOrder < 1 || lineupOrder > 9) {
                    throw new ValidationException(
                            "타자 '" + playerName + "'의 라인업 순서가 유효하지 않습니다: " + lineupOrder + " (1-9 사이여야 합니다).");
                }
                orderedBattersTemp.add(Map.entry(lineupOrder, batter));
            } else if (pitcher != null && normalizeTeamName(pitcher.getTeam()).equalsIgnoreCase(normalizedTeam)) {
                // 투수인 경우
                if (newStartingPitcher != null) {
                    throw new ValidationException("선발 투수는 한 명만 지정할 수 있습니다.");
                }
                newStartingPitcher = pitcher;
            } else {
                throw new ValidationException(
                        "라인업에 포함된 선수 '" + playerName + "'를 팀 '" + request.getTeamName() + "'에서 찾을 수 없거나 소속 팀이 다릅니다.");
            }
        }

        // 타자들을 타순(position)에 따라 정렬
        orderedBattersTemp.sort(Map.Entry.comparingByKey());

        // 정렬된 타자 리스트를 최종 라인업에 추가
        List<Batter> newBattingOrder = new ArrayList<>();
        for (Map.Entry<Integer, Batter> entry : orderedBattersTemp) {
            newBattingOrder.add(entry.getValue());
        }

        // 유효성 검사
        if (newBattingOrder.size() != 9) {
            throw new ValidationException("라인업은 9명의 타자로 정확히 구성되어야 합니다 (현재 " + newBattingOrder.size() + "명).");
        }
        // 타순 중복/누락 검사
        if (orderedBattersTemp.stream().map(Map.Entry::getKey).distinct().count() != 9) {
            throw new ValidationException("타자 라인업 순서가 중복되거나 누락되었습니다.");
        }
        if (newStartingPitcher == null) {
            throw new ValidationException("선발 투수는 필수로 지정되어야 합니다.");
        }

        // 새로운 커스텀 라인업을 메모리에 저장
        userCustomBattingOrders
                .computeIfAbsent(request.getUserId(), k -> new java.util.HashMap<>())
                .put(request.getTeamName(), newBattingOrder);

        userCustomStartingPitchers
                .computeIfAbsent(request.getUserId(), k -> new java.util.HashMap<>())
                .put(request.getTeamName(), newStartingPitcher);

        logger.info("사용자 {}의 팀 {}에 대한 커스텀 라인업이 메모리에 저장되었습니다.", request.getUserId(), request.getTeamName());
    }

    @Override
    public List<TeamLineup> getAllCustomLineups(String userId) {
        List<TeamLineup> allUserCustomLineups = new ArrayList<>();

        Map<String, List<Batter>> userBattingOrders = userCustomBattingOrders.get(userId);
        if (userBattingOrders != null) {
            userBattingOrders.forEach((teamName, batters) -> {
                for (int i = 0; i < batters.size(); i++) {
                    Batter batter = batters.get(i);
                    TeamLineup tl = new TeamLineup();
                    tl.setTeamName(teamName);
                    tl.setUserId(userId);
                    tl.setPosition((i + 1) + "th_Batter");
                    tl.setPlayerName(batter.getName());
                    allUserCustomLineups.add(tl);
                }
            });
        }

        Map<String, Pitcher> userPitchers = userCustomStartingPitchers.get(userId);
        if (userPitchers != null) {
            userPitchers.forEach((teamName, pitcher) -> {
                TeamLineup tlPitcher = new TeamLineup();
                tlPitcher.setTeamName(teamName);
                tlPitcher.setUserId(userId);
                tlPitcher.setPosition("Starting_Pitcher");
                tlPitcher.setPlayerName(pitcher.getName());
                allUserCustomLineups.add(tlPitcher);
            });
        }
        logger.info("사용자 {}의 모든 커스텀 라인업을 메모리에서 조회했습니다.", userId);
        return allUserCustomLineups;
    }

    @Override
    public List<String> getAvailablePlayers(String teamName) {
        String normalizedTeam = normalizeTeamName(teamName);
        // 1) MyBatis 매퍼로 조회 (TeamLineupMapper XML 기준 사용)
        try {
            String dbTeam = resolveDbTeamName(teamName);
            if (teamLineupMapper != null) {
                List<String> names = teamLineupMapper.findAvailablePlayersByTeamBatters(dbTeam);
                if (names != null && !names.isEmpty()) {
                    logger.info("팀 {}의 타자 {}명을 DB에서 조회했습니다.", teamName, names.size());
                    return names;
                }
            }
        } catch (Exception e) {
            logger.warn("팀 {}의 타자 DB 조회 실패: {}", teamName, e.getMessage());
        }

        // 2) 기본 라인업(인메모리 제공)
        String displayTeam = resolveDisplayTeamName(teamName);
        List<String> defaultOrder = ComputerLineupProvider.getDefaultBattingOrder(displayTeam);
        if (defaultOrder != null && !defaultOrder.isEmpty()) {
            logger.info("팀 {}의 타자 {}명을 인메모리에서 조회했습니다.", teamName, defaultOrder.size());
            return defaultOrder;
        }

        // 3) 폴백 없음: 빈 목록 반환
        logger.warn("팀 {}의 타자 정보를 찾을 수 없습니다.", teamName);
        return java.util.Collections.emptyList();
    }

    @Override
    public List<String> getAvailablePitchers(String teamName) {
        String normalizedTeam = normalizeTeamName(teamName);
        // 1) MyBatis 매퍼로 조회 (TeamLineupMapper XML 기준 사용)
        try {
            String dbTeam = resolveDbTeamName(teamName);
            if (teamLineupMapper != null) {
                List<String> names = teamLineupMapper.findAvailablePlayersByTeamPitchers(dbTeam);
                if (names != null && !names.isEmpty()) {
                    logger.info("팀 {}의 투수 {}명을 DB에서 조회했습니다.", teamName, names.size());
                    return names;
                }
            }
        } catch (Exception e) {
            logger.warn("팀 {}의 투수 DB 조회 실패: {}", teamName, e.getMessage());
        }

        // 2) 기본 선발 투수
        String displayTeam2 = resolveDisplayTeamName(teamName);
        String sp = ComputerLineupProvider.getDefaultStartingPitcher(displayTeam2);
        if (sp != null && !sp.isEmpty()) {
            logger.info("팀 {}의 투수 1명을 인메모리에서 조회했습니다.", teamName);
            return java.util.Collections.singletonList(sp);
        }

        // 3) 폴백 없음: 빈 목록 반환
        logger.warn("팀 {}의 투수 정보를 찾을 수 없습니다.", teamName);
        return java.util.Collections.emptyList();
    }

    // 새로운 메서드: 성적 데이터를 포함한 타자 목록 조회
    public List<Batter> getAvailableBattersWithStats(String teamName) {
        try {
            String dbTeam = resolveDbTeamName(teamName);
            if (teamLineupMapper != null) {
                List<Batter> batters = teamLineupMapper.findAvailableBattersByTeam(dbTeam);
                if (batters != null && !batters.isEmpty()) {
                    logger.info("팀 {}의 타자 성적 데이터 {}명을 DB에서 조회했습니다.", teamName, batters.size());
                    return batters;
                }
            }
        } catch (Exception e) {
            logger.warn("팀 {}의 타자 성적 데이터 DB 조회 실패: {}", teamName, e.getMessage());
        }

        // DB 조회 실패 시 기본 라인업으로 폴백
        List<String> names = getAvailablePlayers(teamName);
        List<Batter> fallbackBatters = new ArrayList<>();
        for (String name : names) {
            fallbackBatters.add(new Batter(name, teamName));
        }
        logger.info("팀 {}의 타자 {}명을 폴백으로 생성했습니다.", teamName, fallbackBatters.size());
        return fallbackBatters;
    }

    // 새로운 메서드: 성적 데이터를 포함한 투수 목록 조회
    public List<Pitcher> getAvailablePitchersWithStats(String teamName) {
        try {
            String dbTeam = resolveDbTeamName(teamName);
            if (teamLineupMapper != null) {
                List<Pitcher> pitchers = teamLineupMapper.findAvailablePitchersByTeam(dbTeam);
                if (pitchers != null && !pitchers.isEmpty()) {
                    logger.info("팀 {}의 투수 성적 데이터 {}명을 DB에서 조회했습니다.", teamName, pitchers.size());
                    return pitchers;
                }
            }
        } catch (Exception e) {
            logger.warn("팀 {}의 투수 성적 데이터 DB 조회 실패: {}", teamName, e.getMessage());
        }

        // DB 조회 실패 시 기본 라인업으로 폴백
        List<String> names = getAvailablePitchers(teamName);
        List<Pitcher> fallbackPitchers = new ArrayList<>();
        for (String name : names) {
            fallbackPitchers.add(new Pitcher(name, teamName));
        }
        logger.info("팀 {}의 투수 {}명을 폴백으로 생성했습니다.", teamName, fallbackPitchers.size());
        return fallbackPitchers;
    }

    private String normalizeTeamName(String teamName) {
        if (teamName == null)
            return null;
        String key = teamName.trim();
        String mapped = getOrDefaultIgnoreCase(TEAM_ALIASES, key, null);
        return mapped != null ? mapped : key;
    }

    // DB 조회용 팀명 변환
    private String resolveDbTeamName(String teamName) {
        if (teamName == null)
            return null;
        String normalized = normalizeTeamName(teamName);
        String db = getOrDefaultIgnoreCase(DB_TEAM_NAMES, normalized, null);
        if (db != null)
            return db;
        String key = teamName.trim();
        db = getOrDefaultIgnoreCase(DB_TEAM_NAMES, key, null);
        return db != null ? db : key;
    }

    // ComputerLineupProvider가 기대하는 한글 풀네임으로 변환
    private String resolveDisplayTeamName(String teamName) {
        if (teamName == null)
            return null;
        String normalized = normalizeTeamName(teamName);
        String display = getOrDefaultIgnoreCase(DISPLAY_TEAM_NAMES, normalized, null);
        if (display != null)
            return display;
        String key = teamName.trim();
        display = getOrDefaultIgnoreCase(DISPLAY_TEAM_NAMES, key, null);
        return display != null ? display : normalized;
    }

    private String resolveEnglishTeamName(String teamName) {
        if (teamName == null)
            return null;
        String normalized = normalizeTeamName(teamName);
        String eng = ENGLISH_DISPLAY_NAMES.get(normalized);
        return eng != null ? eng : normalized;
    }

    private String getOrDefaultIgnoreCase(Map<String, String> map, String key, String defaultValue) {
        if (key == null)
            return defaultValue;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return defaultValue;
    }

    public Batter getBatterByName(String playerName) {
        Batter batter = null;
        try {
            if (batterMapper != null) {
                batter = batterMapper.findByName(playerName);
            }
        } catch (Exception ignored) {
        }

        if (batter != null && batter.getAtBats() > 0 && batter.getBattingAverage() == 0.0) {
            batter.setBattingAverage(batter.calculateBattingAverage());
        }
        return batter;
    }

    public Pitcher getPitcherByName(String pitcherName) {
        try {
            if (pitcherMapper != null) {
                Pitcher p = pitcherMapper.findByName(pitcherName);
                if (p != null)
                    return p;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}