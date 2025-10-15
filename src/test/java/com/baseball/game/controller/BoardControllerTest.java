package com.baseball.game.controller;

import com.baseball.game.dto.BoardDetailDto;
import com.baseball.game.dto.BoardDto;
import com.baseball.game.dto.BoardPageResponse;
import com.baseball.game.dto.BoardRequestDto;
import com.baseball.game.dto.CommentDto;
import com.baseball.game.service.BoardService;
import com.baseball.game.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("GET /api/board - 카테고리 없이 페이지 목록 조회")
    void getBoards_withoutCategory() throws Exception {
        ArrayList<BoardDto> list = new ArrayList<>();
        BoardDto dto = new BoardDto();
        dto.setNo(1);
        dto.setTitle("t1");
        list.add(dto);
        BoardPageResponse response = new BoardPageResponse(100, list);
        when(boardService.getPagedList(1, 10)).thenReturn(response);

        mockMvc.perform(get("/api/board")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(100))
                .andExpect(jsonPath("$.list[0].no").value(1))
                .andExpect(jsonPath("$.list[0].title").value("t1"));
    }

    @Test
    @DisplayName("GET /api/board?category=notice - 카테고리 페이지 목록 조회")
    void getBoards_withCategory() throws Exception {
        ArrayList<BoardDto> list = new ArrayList<>();
        BoardDto dto = new BoardDto();
        dto.setNo(2);
        dto.setTitle("t2");
        list.add(dto);
        BoardPageResponse response = new BoardPageResponse(5, list);
        when(boardService.getPagedListCategory(1, 10, "notice")).thenReturn(response);

        mockMvc.perform(get("/api/board")
                .param("page", "1")
                .param("size", "10")
                .param("category", "notice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(5))
                .andExpect(jsonPath("$.list[0].no").value(2))
                .andExpect(jsonPath("$.list[0].title").value("t2"));
    }

    @Test
    @DisplayName("GET /api/board/{no} - 상세 조회(댓글 포함)")
    void getBoard_detail() throws Exception {
        BoardDto board = new BoardDto();
        board.setNo(10);
        board.setTitle("detail");
        ArrayList<CommentDto> comments = new ArrayList<>();
        CommentDto c = new CommentDto();
        c.setCommentId(7);
        c.setText("hi");
        comments.add(c);

        when(boardService.getBoard(10)).thenReturn(board);
        when(commentService.getComment(10)).thenReturn(comments);

        mockMvc.perform(get("/api/board/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board.no").value(10))
                .andExpect(jsonPath("$.board.title").value("detail"))
                .andExpect(jsonPath("$.comments[0].commentId").value(7))
                .andExpect(jsonPath("$.comments[0].text").value("hi"));
    }

    @Test
    @DisplayName("POST /api/board - 게시글 작성")
    void createBoard() throws Exception {
        BoardRequestDto req = new BoardRequestDto();
        req.setTitle("t");
        req.setWriter("w");
        req.setText("x");
        req.setCategory("c");

        mockMvc.perform(post("/api/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(boardService, times(1)).write(any(BoardRequestDto.class));
    }

    @Test
    @DisplayName("PUT /api/board/{no} - 게시글 수정")
    void updateBoard() throws Exception {
        BoardRequestDto req = new BoardRequestDto();
        req.setText("updated");
        req.setWriter("w"); // Add this line

        mockMvc.perform(put("/api/board/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(boardService, times(1)).modify(eq(3), any(BoardRequestDto.class));
    }

    @Test
    @DisplayName("DELETE /api/board/{no} - 게시글 삭제")
    void deleteBoard() throws Exception {
        mockMvc.perform(delete("/api/board/4")
                .param("writer", "w"))
                .andExpect(status().isOk());

        verify(boardService, times(1)).delete(eq(4),eq("w"));
    }
}
