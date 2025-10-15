package com.baseball.game.controller;

import com.baseball.game.dto.MemberDto;
import com.baseball.game.exception.ValidationException;
import com.baseball.game.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private MemberService memberService;

        @Test
        @DisplayName("POST /api/login/login - 로그인 성공 응답")
        void login_success() throws Exception {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("message", "로그인 성공");
                when(memberService.loginProcess(any(MemberDto.class))).thenReturn(resp);

                MemberDto req = new MemberDto();
                req.setId("u1");
                req.setPw("p1");

                mockMvc.perform(post("/api/login/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("로그인 성공"));
        }

        @Test
        @DisplayName("POST /api/login/register - 회원가입 성공 응답")
        void register_success() throws Exception {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("message", "회원가입 성공");
                when(memberService.registerProcess(any(MemberDto.class))).thenReturn(resp);

                MemberDto req = new MemberDto();
                req.setId("u2");
                req.setPw("p2");
                req.setEmail("A");

                mockMvc.perform(post("/api/login/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("회원가입 성공"));
        }

        @Test
        @DisplayName("POST /api/login/login - 아이디 누락 시 400 VALIDATION_ERROR")
        void login_validation_error() throws Exception {
                MemberDto req = new MemberDto();
                req.setPw("p1");
                when(memberService.loginProcess(any(MemberDto.class)))
                                .thenThrow(new ValidationException("아이디는 필수입니다."));

                mockMvc.perform(post("/api/login/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.message").value("아이디는 필수입니다."));
        }

}
