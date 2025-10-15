package com.baseball.game.service;
import com.baseball.game.dto.CommentDto;
import com.baseball.game.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
@Service
public class CommentServiceImpl implements CommentService{
	
	public final CommentMapper mapper;
	
	@Autowired
	public CommentServiceImpl(CommentMapper mapper) {
		this.mapper=mapper;
	}
	
	@Override
	public ArrayList<CommentDto> getComment(int boardNo){
		ArrayList<CommentDto> m=mapper.getComment(boardNo);
		return m;
	}
	@Override
	public void comment(CommentDto d) {
		mapper.comment(d);
	}
	@Override
	public void delete(int boardNo,int commentId) {
		mapper
				.delete(boardNo, commentId);
	}
	@Override
	public void update(int boardNo,int commentId,String text) {
		mapper.update(boardNo, commentId, text);
	}
}
