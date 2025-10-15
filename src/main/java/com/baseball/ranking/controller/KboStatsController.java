package com.baseball.ranking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController; // @RestController 사용
import org.springframework.ui.Model; // Model 객체 추가

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.baseball.ranking.dto.KboHitterStatsDto;
import com.baseball.ranking.dto.KboPitcherStatsDto;
import com.baseball.ranking.dto.KboTeamStatsDto;
import com.baseball.ranking.service.KboStatsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/kbo") // API 경로를 구분하기 위해 @RequestMapping 수정
public class KboStatsController {
    private final KboStatsService kboStatsService;

    @Autowired
    public KboStatsController(KboStatsService kboStatsService) {
        this.kboStatsService = kboStatsService;
    }

    // --- 타자 기록 페이지 ---
    @GetMapping("/hitter-stats")
    public List<KboHitterStatsDto> getHitterStats(@RequestParam(required = false) String sortBy) {
        List<KboHitterStatsDto> hitterStats;
        if (sortBy == null) {
            hitterStats = kboStatsService.getHitterStatsOrderByBattingAverage(); // Default
        } else {
            switch (sortBy) {
                case "numberOfGames":
                    hitterStats = kboStatsService.getHitterStatsOrderByNumberOfGamesDesc();
                    break;
                case "plateAppearance":
                    hitterStats = kboStatsService.getHitterStatsOrderByPlateAppearanceDesc();
                    break;
                case "run":
                    hitterStats = kboStatsService.getHitterStatsOrderByRunDesc();
                    break;
                case "hit":
                    hitterStats = kboStatsService.getHitterStatsOrderByHitDesc();
                    break;
                case "twoBase":
                    hitterStats = kboStatsService.getHitterStatsOrderByTwoBaseDesc();
                    break;
                case "threeBase":
                    hitterStats = kboStatsService.getHitterStatsOrderByThreeBaseDesc();
                    break;
                case "homeRun":
                    hitterStats = kboStatsService.getHitterStatsOrderByHomeRun();
                    break;
                case "runsBattedIn":
                    hitterStats = kboStatsService.getHitterStatsOrderByRunsBattedIn();
                    break;
                case "fourBall":
                    hitterStats = kboStatsService.getHitterStatsOrderByFourBallDesc();
                    break;
                case "strikeOut":
                    hitterStats = kboStatsService.getHitterStatsOrderByStrikeOutDesc();
                    break;
                case "onBasePercentage":
                    hitterStats = kboStatsService.getHitterStatsOrderByOnBasePercentageDesc();
                    break;
                case "ops":
                    hitterStats = kboStatsService.getHitterStatsOrderByOPSDesc();
                    break;
                case "battingAverage":
                default:
                    hitterStats = kboStatsService.getHitterStatsOrderByBattingAverage(); // Default
                    break;
            }
        }
        return hitterStats; // 데이터를 직접 반환
    }

    // --- 투수 기록 페이지 ---
    @GetMapping("/pitcher-stats")
    public List<KboPitcherStatsDto> getPitcherStats(@RequestParam(required = false) String sortBy) {
        List<KboPitcherStatsDto> pitcherStats;
        if (sortBy == null) {
            pitcherStats = kboStatsService.getPitcherStatsOrderByERADesc(); // Default
        } else {
            switch (sortBy) {
                case "numberOfGame":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByNumberOfGameDesc();
                    break;
                case "win":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByWin();
                    break;
                case "lose":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByLoseDesc();
                    break;
                case "save":
                    pitcherStats = kboStatsService.getPitcherStatsOrderBySaveDesc();
                    break;
                case "hold":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByHoldDesc();
                    break;
                case "numberOfInning":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByNumberOfInningDesc();
                    break;
                case "hit":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByHitDesc();
                    break;
                case "homeRun":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByHomeRunDesc();
                    break;
                case "fourBall":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByFourBallDesc();
                    break;
                case "strikeOut":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByStrikeOut();
                    break;
                case "run":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByRunDesc();
                    break;
                case "earnedRun":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByEarnedRunDesc();
                    break;
                case "whip":
                    pitcherStats = kboStatsService.getPitcherStatsOrderByWHIPAsc();
                    break;
                case "era":
                default:
                    pitcherStats = kboStatsService.getPitcherStatsOrderByERADesc(); // Default
                    break;
            }
        }
        return pitcherStats; // 데이터를 직접 반환
    }

    // --- 팀 기록 페이지 ---
    @GetMapping("/team-stats")
    public List<KboTeamStatsDto> getTeamStats(@RequestParam(required = false) String sortBy) {
        List<KboTeamStatsDto> teamStats = kboStatsService.getTeamStatsOrderByWinPercentage(); // 팀 기록은 정렬 옵션이 하나뿐이므로 별도 처리 없이 호출
        return teamStats; // 데이터를 직접 반환
    }
}