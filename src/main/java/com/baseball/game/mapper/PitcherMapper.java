package com.baseball.game.mapper;

import com.baseball.game.dto.Pitcher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PitcherMapper {
    // 팀별 투수 리스트
    List<Pitcher> findByTeam(@Param("team") String team);

    // 이름으로 투수 1명
    Pitcher findByName(@Param("name") String name);

    // No로 투수 1명
    Pitcher findByNo(@Param("no") int no);

    // 여러 이름으로 투수 리스트
    List<Pitcher> findByNames(@Param("names") List<String> names);

    // 팀별 투수 이름만 리스트
    List<String> findNamesByTeam(@Param("team") String team);
}
