package com.baseball.game.util;

import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;
import com.baseball.game.dto.GameDto;
import com.baseball.game.constant.GameConstants;

public class GameLogicUtil {

    /**
     * 게임 진행 중 공통으로 사용되는 순수 로직 모음.
     * - 입력으로 전달된 DTO의 상태를 기반으로 확률/점수 등을 계산하여 결과 문자열 또는 점수를 산출합니다.
     * - 외부 자원 접근(DB/네트워크)이 없어 테스트가 쉽고, 비즈니스 규칙 변경 시 이 클래스만 집중해서 수정할 수 있습니다.
     * 주의: 이 유틸은 상태를 저장하지 않으며, 부수효과가 필요한 경우(GameDto 변경 등)에는 메서드에 명시적으로 나타냅니다.
     */
    /**
     * 투수의 WHIP을 기반으로 control 스탯 (0-100)을 계산합니다.
     * 낮은 WHIP은 높은 control을 의미합니다.
     * 
     * @param whipValue 투수의 실제 WHIP 값
     * @return 계산된 control 스탯 (0-100)
     */
    private static int calculateControlFromWhip(double whipValue) {

        double minWHIP = 1.2;
        double maxWHIP = 1.9;

        double calculatedControl;
        if (whipValue <= minWHIP) {
            calculatedControl = 100.0;
        } else if (whipValue >= maxWHIP) {
            calculatedControl = 0.0;
        } else {
            calculatedControl = 100 - ((whipValue - minWHIP) / (maxWHIP - minWHIP)) * 100;
        }
        return (int) Math.max(0, Math.min(100, calculatedControl));
    }
    // GameLogicUtil.java 파일에 아래 메서드를 추가합니다.

    /**
     * 투수와 타자의 실제 기록을 기반으로 스트라이크/볼 확률을 계산하여 투구 결과를 결정합니다.
     * 1) 스트라이크 확률 = {(투수의 삼진 / 총 상대 타자 수) + (타자의 삼진 / 타석 수)} / 2
     * 2) 볼 확률 = {(투수의 볼넷 + 투수의 HBP) / 총 상대 타자 수) + ((타자의 볼넷 + 타자의 HBP) / 타석 수)} / 2
     * 
     * @param pitcher 현재 투수 객체
     * @param batter  현재 타자 객체
     * @return "스트라이크" 또는 "볼"
     */
    public static String determinePitchResultByStats(Pitcher pitcher, Batter batter) {
        // 목적: 투수/타자 양측의 과거 성적을 기반으로 스트라이크/볼의 확률을 가중 평균하여 단일 투구 결과를 도출
        // 특징: 투수의 삼진/볼넷 성향과 타자의 삼진/볼넷 성향을 동시에 반영하여 극단값을 완화합니다.
        // DTO에 아래와 같은 getter가 있다고 가정합니다.
        // pitcher: getStrikeouts(), getTotalBattersFaced(), getWalks(), getHitByPitch()
        // batter: getStrikeouts(), getPlateAppearances(), getWalks(), getHitByPitch()

        // 투수/타자 데이터가 0인 경우를 대비한 안전장치
        double pitcherTotalBatters = pitcher.getTotalBattersFaced() > 0 ? pitcher.getTotalBattersFaced() : 1;
        double batterTotalPAs = batter.getPlateAppearances() > 0 ? batter.getPlateAppearances() : 1;

        // 1. 스트라이크 확률 계산
        double pitcherStrikeoutRate = pitcher.getStrikeouts() / pitcherTotalBatters;
        double batterStrikeoutRate = batter.getStrikeOut() / batterTotalPAs;
        double finalStrikeProb = (pitcherStrikeoutRate + batterStrikeoutRate) / 2.0;

        // 2. 볼 확률 계산
        double pitcherWalkRate = (pitcher.getWalks() + pitcher.getHitByPitch()) / pitcherTotalBatters;
        double batterWalkRate = (batter.getFourBall() + batter.getHitByPitch()) / batterTotalPAs;
        double finalBallProb = (pitcherWalkRate + batterWalkRate) / 2.0;

        // 3. 최종 확률에 기반한 결과 결정
        // 두 확률의 합을 1로 정규화하여 스트라이크가 나올 확률을 계산
        double totalProb = finalStrikeProb + finalBallProb;
        if (totalProb == 0) {
            return (Math.random() < 0.6) ? "스트라이크" : "볼"; // 데이터가 전혀 없는 경우 기본 확률 (60% 스트라이크)
        }

        double strikeChance = finalStrikeProb / totalProb;

        if (Math.random() < strikeChance) {
            return "스트라이크";
        } else {
            return "볼";
        }
    }

    /**
     * 타자의 타율을 기반으로 contact 스탯 (0-100)을 계산합니다.
     * 높은 타율은 높은 contact를 의미합니다.
     * 
     * @param battingAverage 타자의 실제 타율 (예: 0.250)
     * @return 계산된 contact 스탯 (0-100)
     */
    public static int calculateContactFromBattingAverage(double battingAverage) {
        // 목적: 리그 평균을 중심으로 컨택 값을 동적으로 산출하여 밸런싱
        double avg = GameConstants.LEAGUE_AVG_BA; // 예: 0.260
        double spread = GameConstants.LEAGUE_BA_SPREAD; // 예: 0.080 → [0.180, 0.340]

        double minAvg = Math.max(0.0, avg - spread);
        double maxAvg = Math.min(1.0, avg + spread);
        final double EPS = 1e-9; // 경계 부동소수 비교용

        double calculatedContact;
        if (battingAverage <= minAvg + EPS) {
            calculatedContact = 0.0;
        } else if (battingAverage >= maxAvg - EPS) {
            calculatedContact = 100.0;
        } else {
            calculatedContact = ((battingAverage - minAvg) / (maxAvg - minAvg)) * 100;
        }
        // 반올림으로 경계값(예: 99.999...)을 안정적으로 100 처리
        return (int) Math.round(Math.max(0, Math.min(100, calculatedContact)));
    }

    /**
     * 타자의 홈런 수를 기반으로 power 스탯 (0-100)을 계산합니다.
     * 홈런 수가 많을수록 높은 power를 의미합니다. (단순화된 예시)
     * 
     * @param homeRuns 타자의 실제 홈런 수
     * @return 계산된 power 스탯 (0-100)
     */
    private static int calculatePowerFromHomeRuns(int homeRuns) {
        // 목적: 시즌 누적 홈런 개수를 단순 선형 변환하여 파워 점수로 사용
        // 주의: 파워의 세밀한 보정(경기수, 홈런율 등)은 추후 필요 시 이 메서드를 확장합니다.
        // 홈런 수에 비례하여 파워를 계산. 예를 들어, 50홈런을 치면 파워 100으로 간주
        // 더 정교한 계산을 위해 홈런율 (HR/PA) 등을 사용할 수 있으나, 현재 필드 기준 단순화
        double calculatedPower = homeRuns * 2.0; // 홈런 50개면 파워 100
        return (int) Math.max(0, Math.min(100, calculatedPower));
    }

    /**
     * 투수의 제구력과 프론트엔드에서 선택한 투구 타입에 따라 실제 결과가 바뀌는 투구 결과 결정.
     * 유저가 선택한 공(pitchType)이 투수의 제구력(control)에 따라 성공하거나 낮은 확률로 실패하여 반대 결과가 됩니다.
     * 
     * @param pitcher   현재 투수 객체 (WHIP 스탯 포함)
     * @param pitchType 프론트엔드에서 선택한 투구 타입 ("strike" 또는 "ball")
     * @return "스트라이크" 또는 "볼"
     */
    public static String determinePitchResult(Pitcher pitcher, String pitchType) {
        // 목적: 사용자가 선택한 투구 타입(의도)이 투수의 제구력에 의해 실제 결과로 얼마나 잘 반영되는지 모델링
        // 직관: WHIP이 낮을수록 의도한 결과가 나올 확률이 높음
        // control 스탯을 WHIP으로부터 실시간 계산
        int control = calculateControlFromWhip(pitcher.getWhip());

        // 제구력 50: 70%, 100: 100%, 0: 40% (선수의 제구력 100점은 완벽에 가까움을 의미)
        double baseProb = 0.7 + (control - 50) * 0.006;
        baseProb = Math.max(0.4, Math.min(1.0, baseProb)); // 확률 범위 40% ~ 100%

        double rand = Math.random();
        if (rand < baseProb) { // 제구에 성공하여 의도한 결과가 나옴
            return pitchType.equals("strike") ? "스트라이크" : "볼";
        } else { // 제구에 실패하여 반대 결과가 나옴 (낮은 확률로 바뀜)
            return pitchType.equals("strike") ? "볼" : "스트라이크";
        }
    }

    /**
     * 투수와 타자의 실제 스탯을 기반으로 투구 결과 (스트라이크, 볼, 인플레이)를 결정합니다.
     * 이 메서드는 현재 사용하지 않습니다. (determinePitchResult로 대체)
     */
    /*
     * public static String determinePitchResultByStats(Pitcher pitcher, Batter
     * batter) {
     * // ... (이전과 동일한 주석 처리된 내용) ...
     * }
     */

    /**
     * 타이밍과 투수/타자의 스탯을 반영하여 스윙 시의 타격 결과를 결정합니다.
     * 스윙하지 않으면 투구 타입("스트라이크" 또는 "볼")을 그대로 반환합니다.
     * 스윙 시에는 투수와 타자의 능력치를 기반으로 타격 점수를 계산하고, 이 점수에 따라 안타, 홈런, 아웃 등을 결정합니다.
     *
     * @param swing     스윙 여부 (true = 스윙, false = 노스윙)
     * @param pitcher   현재 투수 객체
     * @param pitchType 투수가 던진 공의 실제 유형 ("strike" 또는 "ball") - 투구의 존 여부
     * @param timing    Boolean(true=보너스, false/NULL=보정 없음) 또는 Double(0.0~1.0, 0.5가
     *                  정타)
     * @param batter    현재 타자 객체
     * @return 타격 결과 문자열 ("스트라이크", "볼", "헛스윙", "안타", "홈런", "삼진 아웃" 등)
     */
    public static String determineHitResultWithTiming(boolean swing, Pitcher pitcher, String pitchType, Object timing,
            Batter batter) {
        // 목적: 스윙을 한 경우, 타자/투수 능력과 타이밍을 조합해 최종 타격 결과를 결정합니다.
        // 설계 의도:
        // - 컨택(=타율 기반)과 파워(=홈런 기반)를 가중 합산해 타격 기본 점수를 구성
        // - 투수 제구력은 페널티로 반영(제구력이 좋을수록 타격 점수에서 불리)
        // - 타이밍은 미세 조정(과도한 랜덤성 보다는 플레이어 입력 영향 강조)
        // 스윙을 하지 않았을 경우, 투구 결과만 반환
        if (!swing) {
            return pitchType.equals("strike") ? "스트라이크" : "볼";
        }
        // timing이 Boolean인 경우: true면 타이밍 보너스 가산, false/null이면 보정 없음
        boolean timingBonus = false;
        double timingDelta;
        if (timing instanceof Boolean) {
            timingBonus = (Boolean) timing;
            timingDelta = 0.25; // 중립값
        } else if (timing instanceof Number) {
            double t = ((Number) timing).doubleValue();
            timingDelta = Math.abs(t - 0.5);
        } else {
            timingDelta = 0.25;
        }
        // 1) 컨택 확률: 배터 K%와 피처 K% 기반 + 존/타이밍 보정
        double batterPA = batter.getPlateAppearances() > 0 ? batter.getPlateAppearances() : 1;
        double batterKRate = Math.max(0.0, Math.min(1.0, (double) batter.getStrikeOut() / batterPA));
        double pitcherBF = pitcher.getTotalBattersFaced() > 0 ? pitcher.getTotalBattersFaced() : 1;
        double pitcherKRate = Math.max(0.0, Math.min(1.0, (double) pitcher.getStrikeouts() / pitcherBF));

        double contactProb = 1.0 - ((batterKRate * 0.5) + (pitcherKRate * 0.5));
        // 존 보정
        contactProb += "strike".equals(pitchType) ? 0.07 : -0.10;
        // 타이밍 보정: 정타에 가까울수록 컨택↑ (위에서 계산한 timingDelta 사용)
        contactProb += (0.2 - timingDelta * 0.4);
        if (timingBonus) {
            contactProb += 0.05; // 타이밍 보너스 소폭 가산
        }
        contactProb = Math.max(0.10, Math.min(0.95, contactProb));

        if (Math.random() > contactProb) {
            return "헛스윙";
        }

        // 2) 인플레이 시 안타 확률: 배터 BA와 투수 피안타율 기반 + 존/타이밍 보정
        double batterBA = batter.getBattingAverage() > 0 ? batter.getBattingAverage()
                : batter.calculateBattingAverage();
        double pitcherHitRate = pitcher.getTotalBattersFaced() > 0
                ? ((double) pitcher.getHits() / pitcher.getTotalBattersFaced())
                : 0.25;
        double pHit = batterBA * 0.6 + pitcherHitRate * 0.4; // 기본 가중 평균
        pHit += "strike".equals(pitchType) ? 0.03 : -0.05; // 존 보정
        pHit += (0.1 - timingDelta * 0.2);
        if (timingBonus) {
            pHit += 0.03; // 보너스 시 안타 확률 소폭 증가
        }
        pHit += GameConstants.HIT_SCORE_BIAS / 100.0; // 전역 상향치(점수→확률 환산)
        pHit = Math.max(0.12, Math.min(0.60, pHit));

        if (Math.random() >= pHit) {
            // 아웃 유형 결정: 파워 낮으면 GB↑, 높으면 FB↑
            int power = calculatePowerFromHomeRuns(batter.getHomeRuns());
            double gbProb = 0.55 - power * 0.003 + ("strike".equals(pitchType) ? -0.03 : 0.03) + (timingDelta * 0.1);
            gbProb = Math.max(0.25, Math.min(0.75, gbProb));
            return Math.random() < gbProb ? "땅볼 아웃" : "뜬공 아웃";
        }

        // 3) 안타일 때 단타/장타 분배: 실제 분포 기반 + 타이밍에 따른 장타 가중
        int hits = Math.max(0, batter.getHits());
        int doubles = Math.max(0, batter.getTwoBases());
        int triples = Math.max(0, batter.getThreeBases());
        int homers = Math.max(0, batter.getHomeRuns());

        double base = Math.max(1.0, hits); // 분모 0 방지
        double w2B = doubles / base;
        double w3B = triples / base;
        double wHR = homers / base;
        double w1B = Math.max(0.0, 1.0 - (w2B + w3B + wHR));

        // 타이밍 스위트스팟일수록 장타 가중 강화
        double sweet = Math.max(0.0, 1.0 - (timingDelta * 10.0)); // 0~1 (정타에 가까울수록 1)
        if (timingBonus) {
            sweet = Math.min(1.0, sweet + 0.1);
        }
        double boost = 0.25 * sweet; // 최대 +25%
        double hrBoost = 1.0 + boost * 0.6; // HR에 더 큰 가중
        double t3Boost = 1.0 + boost * 0.4;
        double t2Boost = 1.0 + boost * 0.3;
        double s1Boost = 1.0 - boost * 0.4; // 정타면 단타 비중 소폭 감소

        wHR *= hrBoost;
        w3B *= t3Boost;
        w2B *= t2Boost;
        w1B *= s1Boost;

        // 정규화
        double sum = w1B + w2B + w3B + wHR;
        if (sum <= 0) {
            // 분포가 비정상일 경우 안전하게 단타 처리
            return "안타";
        }
        w1B /= sum;
        w2B /= sum;
        w3B /= sum;
        wHR /= sum;

        double r = Math.random();
        if (r < wHR)
            return "홈런";
        if (r < wHR + w3B)
            return "3루타";
        if (r < wHR + w3B + w2B)
            return "2루타";
        return "안타";
    }

    /**
     * 땅볼 처리: 병살/진루/아웃
     * GameDto의 out, bases를 직접 변경합니다.
     * 
     * @param game   현재 게임 DTO
     * @param batter 타자
     * @return "땅볼 아웃" 또는 "병살타"
     */
    public static String processGroundBall(GameDto game, Batter batter) {
        // 목적: 땅볼 안타/아웃 상황에서 병살 가능성 및 아웃 카운트 증가를 처리
        // 특징: 2아웃 시에는 추가 처리 없이 이닝 종료까지 고려, 1루 주자 존재 시 30% 확률로 병살 처리
        Batter[] bases = game.getBases();
        int currentOuts = game.getOut();
        // 스냅샷(포스 여부 판단용)
        boolean preFirst = bases[1] != null;
        boolean preSecond = bases[2] != null;
        boolean preThird = bases[3] != null;

        if (currentOuts == 2) {
            game.setOut(currentOuts + 1);
            return "땅볼 아웃";
        }
        if (bases[1] != null) {
            if (Math.random() < 0.3) {
                // 병살타: 타자와 1루 주자 아웃
                game.setOut(currentOuts + 2);
                // 1루 주자 제거
                Batter firstRunner = bases[1];
                bases[1] = null;
                // baseRunners는 제거. 주자 배열만 관리
                // 강제 주자만 1베이스 진루(득점 반영 포함). 1루 주자는 아웃이므로 이동 제외
                advanceForcedRunnersWithSnapshot(game, preFirst, preSecond, preThird, false);
                return "병살타";
            } else {
                game.setOut(currentOuts + 1);
                // 일반 땅볼 아웃: 타자 아웃 + 강제 주자만 1베이스 진루(득점 반영)
                advanceForcedRunnersWithSnapshot(game, preFirst, preSecond, preThird, true);
                return "땅볼 아웃";
            }
        } else {
            game.setOut(currentOuts + 1);
            return "땅볼 아웃";
        }
    }

    // 강제 진루만 처리하는 유틸리티 (스냅샷 기반)
    private static void advanceForcedRunnersWithSnapshot(GameDto game,
            boolean preFirst,
            boolean preSecond,
            boolean preThird,
            boolean includeFirstRunnerAdvance) {
        Batter[] oldBases = game.getBases();
        Batter[] newBases = new Batter[4];
        // 초기화: 현재 상태를 복사
        newBases[1] = oldBases[1];
        newBases[2] = oldBases[2];
        newBases[3] = oldBases[3];

        // 3루 주자: 1,2,3루 모두 점유(사전)였다면 홈으로 강제 진루 → 득점
        if (preThird && preSecond && preFirst && oldBases[3] != null) {
            // 득점 처리
            if (game.isTop()) {
                game.setAwayScore(game.getAwayScore() + 1);
            } else {
                game.setHomeScore(game.getHomeScore() + 1);
            }
            newBases[3] = null; // 홈인
        }

        // 2루 주자: 사전에 1루도 점유되어 있었다면 3루로 강제 진루
        if (preSecond && preFirst && oldBases[2] != null) {
            // 3루가 비어 있으면 2루 주자를 3루로 이동
            newBases[3] = (newBases[3] != null) ? newBases[3] : oldBases[2];
            newBases[2] = (newBases[3] == oldBases[2]) ? null : newBases[2];
        }

        // 1루 주자: 일반 땅볼에서는 2루로 강제 진루, 병살에서는 제외
        if (includeFirstRunnerAdvance && preFirst && oldBases[1] != null) {
            // 2루가 비어 있으면 1루 주자를 2루로 이동
            if (newBases[2] == null) {
                newBases[2] = oldBases[1];
                newBases[1] = null;
            }
        }

        // 베이스 갱신 (주자 목록은 bases로부터 계산 가능)
        game.setBases(newBases);
    }

    /**
     * 모든 베이스 주자를 초기화합니다.
     * 
     * @param game 현재 게임 DTO
     */
    public static void resetBases(GameDto game) {
        // 목적: 모든 루 상황을 초기화하여 이닝 시작/홈런/병살 등 특정 이벤트 이후 상태를 정리
        game.setBases(new Batter[4]);
    }

    /**
     * 특정 베이스에 주자를 추가합니다. (예: 타자가 안타 치고 1루 진루)
     * 
     * @param game   현재 게임 DTO
     * @param base   주자가 위치할 베이스 (1: 1루, 2: 2루, 3: 3루)
     * @param runner 해당 주자 Batter 객체
     */
    public static void addRunnerToBase(GameDto game, int base, Batter runner) {
        // 목적: 타격 결과에 따라 특정 루에 주자를 배치
        // 주의: 베이스 범위는 1~3만 허용(0은 홈, 배열 크기는 4)
        if (base >= 1 && base <= 3) {
            Batter[] bases = game.getBases();
            bases[base] = runner;
            // baseRunners는 유지하지 않음
        }
    }

    /**
     * 볼넷 처리: 1루가 비어 있으면 타자만 1루 진루, 1루가 차 있으면 강제 주자만 1베이스씩 진루 후 타자를 1루에 배치합니다.
     * 득점은 강제 진루에 의해 홈을 밟는 경우에만 가산됩니다.
     *
     * @param game   현재 게임 DTO
     * @param batter 볼넷을 얻은 타자
     */
    public static void processWalk(GameDto game, Batter batter) {
        Batter[] oldBases = game.getBases();
        boolean preFirst = oldBases[1] != null;
        boolean preSecond = oldBases[2] != null;
        boolean preThird = oldBases[3] != null;

        Batter[] newBases = new Batter[4];
        newBases[1] = oldBases[1];
        newBases[2] = oldBases[2];
        newBases[3] = oldBases[3];

        // 강제 진루만 적용
        // 3루 주자 득점 여부 (모두 점유되어 있었다면 강제 홈인)
        if (preThird && preSecond && preFirst && oldBases[3] != null) {
            if (game.isTop()) {
                game.setAwayScore(game.getAwayScore() + 1);
            } else {
                game.setHomeScore(game.getHomeScore() + 1);
            }
            newBases[3] = null;
        }

        // 2루 주자 → 3루 (1루도 점유되어 있었다면 강제)
        if (preSecond && preFirst && oldBases[2] != null) {
            if (newBases[3] == null) {
                newBases[3] = oldBases[2];
                newBases[2] = null;
            }
        }

        // 1루 주자 → 2루 (1루가 점유되어 있었다면 강제)
        if (preFirst && oldBases[1] != null) {
            if (newBases[2] == null) {
                newBases[2] = oldBases[1];
                newBases[1] = null;
            }
        }

        // 타자 1루 진루
        newBases[1] = batter;

        // 베이스 갱신
        game.setBases(newBases);
    }

    /**
     * 루상 주자를 지정된 베이스 수만큼 진루시킵니다.
     * 홈인 시 점수를 증가시킵니다.
     * 
     * @param game           현재 게임 DTO
     * @param basesToAdvance 진루시킬 베이스 수
     */
    public static void advanceRunners(GameDto game, int basesToAdvance) {
        // 목적: 주자들을 뒤에서 앞으로 이동시키며 홈인한 주자 수만큼 득점을 반영
        // 구현 포인트:
        // - 3루부터 역순으로 이동시켜 중복 덮어쓰기를 방지
        // - 홈을 넘는 경우(team 공격/수비에 따라) 점수 가산
        Batter[] oldBases = game.getBases();
        Batter[] newBases = new Batter[4];
        // 주자 목록은 필요 시 bases에서 계산

        for (int i = 3; i >= 1; i--) {
            if (oldBases[i] != null) {
                Batter runner = oldBases[i];
                int newBasePosition = i + basesToAdvance;

                if (newBasePosition >= 4) {
                    if (game.isTop()) {
                        game.setAwayScore(game.getAwayScore() + 1);
                    } else {
                        game.setHomeScore(game.getHomeScore() + 1);
                    }
                } else {
                    newBases[newBasePosition] = runner;
                }
            }
        }
        game.setBases(newBases);
    }
}