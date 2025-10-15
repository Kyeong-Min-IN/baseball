package com.baseball.game.controller;

import com.baseball.game.dto.MemberDto;
import com.baseball.game.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/member")
@CrossOrigin(origins = "http://localhost:3000")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable String id, @RequestBody MemberDto memberDto) {
        memberDto.setId(id);
        try {
            memberService.updateMember(memberDto);
            return ResponseEntity.ok().body(Map.of("success", true, "message", "회원 정보가 수정되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable String id) {
        try {
            memberService.deleteMember(id);
            return ResponseEntity.ok().body(Map.of("success", true, "message", "회원 탈퇴가 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
