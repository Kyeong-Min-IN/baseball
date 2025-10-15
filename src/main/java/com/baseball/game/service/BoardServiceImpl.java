package com.baseball.game.service;

import com.baseball.game.dto.BoardDto;
import com.baseball.game.dto.BoardRequestDto;
import com.baseball.game.dto.BoardPageResponse;
import com.baseball.game.mapper.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
public class BoardServiceImpl implements BoardService {

	private final BoardMapper mapper;

	@Autowired
	public BoardServiceImpl(BoardMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public ArrayList<BoardDto> getListCategory(String category) {
		ArrayList<BoardDto> m = mapper.getListCategory(category);
		return m;
	}

	@Override
	public ArrayList<BoardDto> getList() {
		ArrayList<BoardDto> m = mapper.getList();
		return m;
	}

	@Override
	public void write(BoardRequestDto d) {
		mapper.write(d);
	}

    @Override
    public void modify(int no, BoardRequestDto requestDto) {
        BoardDto boardDto = new BoardDto();
        boardDto.setNo(no);
        boardDto.setTitle(requestDto.getTitle());
        boardDto.setText(requestDto.getText());
        mapper.modify(boardDto);
    }

	@Override
	public void delete(int no, String writer) {
		int deleted = mapper.delete(no, writer);
		if (deleted == 0) {
			throw new RuntimeException("FORBIDDEN: writer mismatch or post not found");
		}
	}

	@Override
	public BoardDto getBoard(int no) {
		return mapper.getBoard(no);
	}

	@Override
	public BoardPageResponse getPagedList(int page, int size) {
		int offset = (page - 1) * size;
		ArrayList<BoardDto> list = mapper.getPagedList(offset, size);
		int totalCount = mapper.getTotalCount();
		return new BoardPageResponse(totalCount, list);
	}

	@Override
	public BoardPageResponse getPagedListCategory(int page, int size, String category) {
		int offset = (page - 1) * size;
		ArrayList<BoardDto> list = mapper.getPagedListCate(offset, size, category);
		int totalCount = mapper.getTotalCountCate(category);
		return new BoardPageResponse(totalCount, list);
	}

	@Override
	public BoardPageResponse getPagedListSearch(int page, int size, String keyword) {
		int offset = (page - 1) * size;
		ArrayList<BoardDto> list = mapper.getPagedListSearch(offset, size, keyword);
		int totalCount = mapper.getTotalCountSearch(keyword);
		return new BoardPageResponse(totalCount, list);
	}

	@Override
	public BoardPageResponse getPagedListCategorySearch(int page, int size, String category, String keyword) {
		int offset = (page - 1) * size;
		ArrayList<BoardDto> list = mapper.getPagedListCateSearch(offset, size, category, keyword);
		int totalCount = mapper.getTotalCountCateSearch(category, keyword);
		return new BoardPageResponse(totalCount, list);
	}
}
