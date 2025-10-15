package com.baseball.game.service;
import java.util.ArrayList;
import com.baseball.game.dto.CommentDto;

public interface CommentService {
	public ArrayList<CommentDto> getComment(int boardNo);
	public void comment(CommentDto d);
	public void delete(int boardNo,int commentId);
	public void update(int boardNo,int commentId,String text);
}
