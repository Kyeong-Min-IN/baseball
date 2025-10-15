package com.baseball.game.mapper;

import com.baseball.game.dto.Pitcher;
import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Player;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeamMapper {
    List<Player> selectPlayersByTeam(String teamId);

    List<Batter> selectBattersByTeam(String teamId);

    List<Pitcher> selectPitchersByTeam(String teamId);

    void registerLineup(@Param("gameId") String gameId, @Param("battingOrder") List<String> battingOrder, @Param("startPitcher") String startPitcher);
}
