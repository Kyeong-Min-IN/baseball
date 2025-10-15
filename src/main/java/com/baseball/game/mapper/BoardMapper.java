package com.baseball.game.mapper;

import com.baseball.game.dto.BoardDto;
import com.baseball.game.dto.BoardRequestDto;
import java.util.ArrayList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardMapper {
	public ArrayList<BoardDto> getList();

	public ArrayList<BoardDto> getListCategory(String category);

	public void write(BoardRequestDto d);

	public int modify(BoardDto boardDto);

	public int delete(@Param("no") int no, @Param("writer") String writer);

	public BoardDto getBoard(int no);

	ArrayList<BoardDto> getPagedList(@Param("offset") int offset, @Param("size") int size);

	ArrayList<BoardDto> getPagedListCate(@Param("offset") int offset, @Param("size") int size,
			@Param("category") String category);

	int getTotalCount();

	int getTotalCountCate(@Param("category") String category);

	// Search-enabled pagination
	ArrayList<BoardDto> getPagedListSearch(@Param("offset") int offset, @Param("size") int size,
			@Param("keyword") String keyword);

	ArrayList<BoardDto> getPagedListCateSearch(@Param("offset") int offset, @Param("size") int size,
			@Param("category") String category, @Param("keyword") String keyword);

	int getTotalCountSearch(@Param("keyword") String keyword);

	int getTotalCountCateSearch(@Param("category") String category, @Param("keyword") String keyword);
}
