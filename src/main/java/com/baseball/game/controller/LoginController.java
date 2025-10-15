package com.baseball.game.controller;

import com.baseball.game.dto.MemberDto;
import com.baseball.game.service.MemberService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/login")
@CrossOrigin(origins = "*")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final MemberService service;

    @Autowired
    public LoginController(MemberService service) {
        this.service = service;
    }

    // 로그인 (Spring Security의 loginProcessingUrl과 일치하도록 경로 수정)
    @PostMapping
    public Map<String, Object> login(@RequestBody MemberDto memberDto) {
        // Spring Security가 인증을 처리하므로, 여기서는 추가적인 로직이 필요할 때만 사용
        // 현재는 Spring Security의 successHandler/failureHandler가 응답을 처리
        logger.info("LoginController: 로그인 요청 수신 - {}", memberDto.getId());
        return service.loginProcess(memberDto);
    }

    // 회원가입
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody MemberDto memberDto) {
        // 모든 검증 및 로직을 서비스로 위임
        return service.registerProcess(memberDto);
    }
}