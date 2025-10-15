package com.baseball.game.service;

import com.baseball.game.dto.CommentDto;
import com.baseball.game.mapper.CommentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    @DisplayName("getComment - 목록 반환")
    void getComment() {
        ArrayList<CommentDto> list = new ArrayList<>();
        CommentDto c = new CommentDto();
        c.setCommentId(1);
        list.add(c);
        when(commentMapper.getComment(5)).thenReturn(list);

        ArrayList<CommentDto> result = commentService.getComment(5);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCommentId()).isEqualTo(1);
    }

    @Test
    @DisplayName("comment - 매퍼 호출")
    void comment() {
        CommentDto d = new CommentDto();
        d.setText("x");
        commentService.comment(d);
        verify(commentMapper, times(1)).comment(any(CommentDto.class));
    }

    @Test
    @DisplayName("delete - 매퍼 호출")
    void deleteComment() {
        commentService.delete(3, 8);
        verify(commentMapper, times(1)).delete(3, 8);
    }

    @Test
    @DisplayName("update - 매퍼 호출")
    void updateComment() {
        commentService.update(2, 9, "t");
        verify(commentMapper, times(1)).update(eq(2), eq(9), eq("t"));
    }
}
