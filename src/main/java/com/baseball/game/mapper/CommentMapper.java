package com.baseball.game.mapper;

import com.baseball.game.dto.CommentDto;
import java.util.ArrayList;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper {
	public ArrayList<CommentDto> getComment(int boardNo);

	public void comment(CommentDto d);

	public void delete(int boardNo, int commentId);

	public void update(int boardNo, int commentId, String text);
}
