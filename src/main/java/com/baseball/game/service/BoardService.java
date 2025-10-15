package com.baseball.game.service;

import com.baseball.game.dto.BoardDto;
import com.baseball.game.dto.BoardRequestDto;
import com.baseball.game.dto.BoardPageResponse;
import java.util.ArrayList;

public interface BoardService {
	public ArrayList<BoardDto> getList();

	public ArrayList<BoardDto> getListCategory(String category);

	public void write(BoardRequestDto d);

	public void modify(int no, BoardRequestDto requestDto);

	public void delete(int no, String writer);

	public BoardDto getBoard(int no);

	public BoardPageResponse getPagedList(int page, int size);

	public BoardPageResponse getPagedListCategory(int page, int size, String category);

	public BoardPageResponse getPagedListSearch(int page, int size, String keyword);

	public BoardPageResponse getPagedListCategorySearch(int page, int size, String category, String keyword);
}
