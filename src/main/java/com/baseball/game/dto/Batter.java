package com.baseball.game.dto;

public class Batter extends Player {
    private int no;
    private int gameNum;
    private int plateAppearances; // 타석
    private int atBats; // 타수
    private int run; // 득점
    private int hits; // 안타
    private int twoBases; // 2루타
    private int threeBases; // 3루타
    private int homeRuns; // 홈런
    private int totalBases; // 총 루타
    private int runsBattedIn; // 타점
    private int sacrificeBunts; // 희생번트
    private int sacrificeFly; // 희생플라이
    private int fourBall; // 볼넷
    private int ibb; // 고의사구
    private int hitByPitch; // 사구
    private int strikeOut; // 삼진
    private int doubleOut; // 병살타
    private double battingAverage; // 타율
    private double slugging; // 장타율
    private double onBasePercentage; // 출루율
    private double onbasePlusSlug; // OPS
    private int multiHit; // 멀티히트
    private double scoringPositionAvg; // 득점권 타율
    private double pinchHitAvg; // 대타 타율
    private int power;
    private int contact;
    private int speed;
    private int eye;

    public Batter() {
    }

    public Batter(String name, String team) {
        this.setName(name);
        this.setTeam(team);
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getGameNum() {
        return gameNum;
    }

    public void setGameNum(int gameNum) {
        this.gameNum = gameNum;
    }

    public int getPlateAppearances() {
        return plateAppearances;
    }

    public void setPlateAppearances(int plateAppearances) {
        this.plateAppearances = plateAppearances;
    }

    public int getAtBats() {
        return atBats;
    }

    public void setAtBats(int atBats) {
        this.atBats = atBats;
    }

    public int getRun() {
        return run;
    }

    public void setRun(int run) {
        this.run = run;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public int getTwoBases() {
        return twoBases;
    }

    public void setTwoBases(int twoBases) {
        this.twoBases = twoBases;
    }

    public int getThreeBases() {
        return threeBases;
    }

    public void setThreeBases(int threeBases) {
        this.threeBases = threeBases;
    }

    public int getHomeRuns() {
        return homeRuns;
    }

    public void setHomeRuns(int homeRuns) {
        this.homeRuns = homeRuns;
    }

    public int getTotalBases() {
        return totalBases;
    }

    public void setTotalBases(int totalBases) {
        this.totalBases = totalBases;
    }

    public int getRunsBattedIn() {
        return runsBattedIn;
    }

    public void setRunsBattedIn(int runsBattedIn) {
        this.runsBattedIn = runsBattedIn;
    }

    public int getSacrificeBunts() {
        return sacrificeBunts;
    }

    public void setSacrificeBunts(int sacrificeBunts) {
        this.sacrificeBunts = sacrificeBunts;
    }

    public int getSacrificeFly() {
        return sacrificeFly;
    }

    public void setSacrificeFly(int sacrificeFly) {
        this.sacrificeFly = sacrificeFly;
    }

    public int getFourBall() {
        return fourBall;
    }

    public void setFourBall(int fourBall) {
        this.fourBall = fourBall;
    }

    public int getIbb() {
        return ibb;
    }

    public void setIbb(int ibb) {
        this.ibb = ibb;
    }

    public int getHitByPitch() {
        return hitByPitch;
    }

    public void setHitByPitch(int hitByPitch) {
        this.hitByPitch = hitByPitch;
    }

    public int getStrikeOut() {
        return strikeOut;
    }

    public void setStrikeOut(int strikeOut) {
        this.strikeOut = strikeOut;
    }

    public int getDoubleOut() {
        return doubleOut;
    }

    public void setDoubleOut(int doubleOut) {
        this.doubleOut = doubleOut;
    }

    public double getBattingAverage() {
        return battingAverage;
    }

    public void setBattingAverage(double battingAverage) {
        this.battingAverage = battingAverage;
    }

    public double getSlugging() {
        return slugging;
    }

    public void setSlugging(double slugging) {
        this.slugging = slugging;
    }

    public double getOnBasePercentage() {
        return onBasePercentage;
    }

    public void setOnBasePercentage(double onBasePercentage) {
        this.onBasePercentage = onBasePercentage;
    }

    public void setOnbasePlusSlug(double onbasePlusSlug) {
        this.onbasePlusSlug = onbasePlusSlug;
    }

    public int getMultiHit() {
        return multiHit;
    }

    public void setMultiHit(int multiHit) {
        this.multiHit = multiHit;
    }

    public double getScoringPositionAvg() {
        return scoringPositionAvg;
    }

    public void setScoringPositionAvg(double scoringPositionAvg) {
        this.scoringPositionAvg = scoringPositionAvg;
    }

    public double getPinchHitAvg() {
        return pinchHitAvg;
    }

    public void setPinchHitAvg(double pinchHitAvg) {
        this.pinchHitAvg = pinchHitAvg;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getContact() {
        return contact;
    }

    public void setContact(int contact) {
        this.contact = contact;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getEye() {
        return eye;
    }

    public void setEye(int eye) {
        this.eye = eye;
    }

    // 타율 계산 메서드
    public double calculateBattingAverage() {
        return atBats > 0 ? (double) hits / atBats : 0.0;
    }

    // 출루율 계산 메서드
    public double calculateOnBasePercentage() {
        if (plateAppearances == 0)
            return 0.0;
        return (double) (hits + fourBall + hitByPitch) / plateAppearances;
    }

    // 장타율 계산 메서드
    public double calculateSluggingPercentage() {
        if (atBats == 0)
            return 0.0;
        int totalBases = hits + (twoBases * 2) + (threeBases * 3) + (homeRuns * 4);
        return (double) totalBases / atBats;
    }

    // OPS 계산 메서드
    public double calculateOPS() {
        return calculateOnBasePercentage() + calculateSluggingPercentage();
    }
}