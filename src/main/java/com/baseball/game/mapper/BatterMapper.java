package com.baseball.game.mapper;

import com.baseball.game.dto.Batter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BatterMapper {
    // 팀별 타자 리스트
    List<Batter> findByTeam(@Param("team") String team);

    // 이름으로 타자 1명
    Batter findByName(@Param("name") String name);

    // No로 타자 1명
    Batter findByNo(@Param("no") int no);

    // 여러 이름으로 타자 리스트
    List<Batter> findByNames(@Param("names") List<String> names);
}
