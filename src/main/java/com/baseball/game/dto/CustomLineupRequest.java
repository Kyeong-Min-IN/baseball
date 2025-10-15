package com.baseball.game.dto;

import lombok.Data;
import java.util.List;

@Data
public class CustomLineupRequest {
    private String userId;
    private String teamName;
    private List<LineupPosition> lineup; // 9명의 타순 정보

    @Data
    public static class LineupPosition {
        private Integer position; // 타순 (1~9)
        private String playerName;
        private Integer playerNo;
    }
}