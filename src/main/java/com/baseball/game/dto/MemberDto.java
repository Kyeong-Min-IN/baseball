package com.baseball.game.dto;

import lombok.Data;

@Data
public class MemberDto {
	private String id;
	private String pw;
	private String email;
	private String nickname;
	private int Game;
	private int Win;
	private int Lose;
	private int Draw;
}
