package com.baseball.game;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.baseball.game", "com.baseball.ranking"})
@MapperScan({"com.baseball.game.mapper", "com.baseball.ranking.mapper"})
public class GameApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameApplication.class, args);
    }
}