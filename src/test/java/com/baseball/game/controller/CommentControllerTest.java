package com.baseball.game.controller;

import com.baseball.game.dto.CommentDto;
import com.baseball.game.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("POST /api/comment - 댓글 작성 201 반환")
    void createComment() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setBoardNo(1);
        dto.setText("hello");

        mockMvc.perform(post("/api/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(commentService, times(1)).comment(any(CommentDto.class));
    }

    @Test
    @DisplayName("PUT /api/comment/{boardNo}/{commentId} - 댓글 수정")
    void updateComment() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setText("upd");

        mockMvc.perform(put("/api/comment/2/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(commentService, times(1)).update(eq(2), eq(9), eq("upd"));
    }

    @Test
    @DisplayName("DELETE /api/comment/{boardNo}/{commentId} - 댓글 삭제")
    void deleteComment() throws Exception {
        mockMvc.perform(delete("/api/comment/3/5"))
                .andExpect(status().isOk());

        verify(commentService, times(1)).delete(eq(3), eq(5));
    }
}
