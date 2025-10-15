package com.baseball.game.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineupRequest {
    private String teamName;
    private List<String> battingOrder;
    private String startingPitcher;
    private List<Integer> battingOrderIds;
    private Integer pitcherId;
    private String pitcherName;
}
