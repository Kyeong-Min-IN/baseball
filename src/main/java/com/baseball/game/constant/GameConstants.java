package com.baseball.game.constant;

public final class GameConstants {

    private GameConstants() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

    // 게임 설정
    public static final int MIN_INNINGS = 3;
    public static final int MAX_INNINGS = 9;
    public static final int MAX_OUTS = 3;
    public static final int MAX_STRIKES = 3;
    public static final int MAX_BALLS = 4;
    public static final int MAX_BASES = 4;
    public static final int PLAYERS_PER_TEAM = 9;

    // 밸런싱 설정 (리그 평균 기반 동적 조정)
    // 리그 평균 타율 (기본값 예: KBO 약 0.260)
    public static final double LEAGUE_AVG_BA = 0.277d;
    // 리그 평균 주변의 타율 범위 폭(±) — 컨택 계산에 사용
    public static final double LEAGUE_BA_SPREAD = 0.073d; // 상한을 0.35 근처로 맞춰 테스트 기대치 반영
    // 리그 평균 변화에 따른 타격 결과 임계값 민감도 (점수 단위)
    // 예: 평균이 0.02 높아지면 임계값을 2점 낮춤 (감도를 100으로 설정 시)
    public static final double LEAGUE_THRESHOLD_SENSITIVITY = 100.0d;

    // 전역 타격 점수 가산치(안타 비율 상향용 간단한 노브)
    public static final double HIT_SCORE_BIAS = 7.0d; // 필요 시 3~8 범위로 조정 권장

    // 결과 다양성을 위한 최종 점수 지터(± 범위)
    public static final double HIT_SCORE_JITTER = 3.0d; // 0~5 권장

    // 베이스 관련
    public static final int HOME_BASE = 0;
    public static final int FIRST_BASE = 1;
    public static final int SECOND_BASE = 2;
    public static final int THIRD_BASE = 3;

    // 투구 타입
    public static final String PITCH_TYPE_STRIKE = "strike";
    public static final String PITCH_TYPE_BALL = "ball";

    // 게임 상태
    public static final String GAME_STATE_TOP = "top";
    public static final String GAME_STATE_BOTTOM = "bottom";

    // 에러 메시지
    public static final String ERROR_GAME_NOT_FOUND = "게임을 찾을 수 없습니다.";
    public static final String ERROR_INVALID_GAME_STATE = "잘못된 게임 상태입니다.";
    public static final String ERROR_VALIDATION_FAILED = "입력값 검증에 실패했습니다.";
}