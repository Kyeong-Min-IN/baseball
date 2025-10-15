package com.baseball.game.service;

import com.baseball.game.dto.BoardDto;
import com.baseball.game.dto.BoardPageResponse;
import com.baseball.game.dto.BoardRequestDto;
import com.baseball.game.mapper.BoardMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @Mock
    private BoardMapper boardMapper;

    @InjectMocks
    private BoardServiceImpl boardService;

    @Test
    @DisplayName("getPagedList - 오프셋 계산 및 totalCount 포함")
    void getPagedList() {
        ArrayList<BoardDto> list = new ArrayList<>();
        BoardDto dto = new BoardDto();
        dto.setNo(1);
        dto.setTitle("t");
        list.add(dto);
        when(boardMapper.getPagedList(0, 10)).thenReturn(list);
        when(boardMapper.getTotalCount()).thenReturn(42);

        BoardPageResponse resp = boardService.getPagedList(1, 10);

        assertThat(resp.getTotalCount()).isEqualTo(42);
        assertThat(resp.getList()).hasSize(1);
        assertThat(resp.getList().get(0).getNo()).isEqualTo(1);
    }

    @Test
    @DisplayName("getPagedListCategory - 카테고리 포함 페이징")
    void getPagedListCategory() {
        ArrayList<BoardDto> list = new ArrayList<>();
        when(boardMapper.getPagedListCate(10, 10, "notice")).thenReturn(list);
        when(boardMapper.getTotalCountCate("notice")).thenReturn(5);

        BoardPageResponse resp = boardService.getPagedListCategory(2, 10, "notice");
        assertThat(resp.getTotalCount()).isEqualTo(5);
        assertThat(resp.getList()).isSameAs(list);
    }

    @Test
    @DisplayName("write - 매퍼 호출")
    void write() {
        BoardRequestDto req = new BoardRequestDto();
        req.setTitle("t");
        req.setWriter("w");
        req.setText("x");
        req.setCategory("c");

        boardService.write(req);
        verify(boardMapper, times(1)).write(any(BoardRequestDto.class));
    }

    @Test
    @DisplayName("modify - 매퍼 호출")
    void modify() {
        BoardRequestDto requestDto = new BoardRequestDto();
        requestDto.setTitle("updated");
        requestDto.setText("new content");

        BoardDto boardDto = new BoardDto();
        boardDto.setNo(3);
        boardDto.setTitle("updated");
        boardDto.setText("new content");

        doNothing().when(boardMapper).modify(any(BoardDto.class));
        boardService.modify(3, requestDto);
        verify(boardMapper, times(1)).modify(eq(boardDto));
    }

    @Test
    @DisplayName("delete - 매퍼 호출")
    void delete() {
        when(boardMapper.delete(anyInt(), anyString())).thenReturn(1);
        boardService.delete(7,"w");
        verify(boardMapper, times(1)).delete(7,"w");
    }

    @Test
    @DisplayName("getBoard - 매퍼 반환 전달")
    void getBoard() {
        BoardDto dto = new BoardDto();
        dto.setNo(9);
        when(boardMapper.getBoard(9)).thenReturn(dto);

        BoardDto result = boardService.getBoard(9);
        assertThat(result.getNo()).isEqualTo(9);
    }
}
