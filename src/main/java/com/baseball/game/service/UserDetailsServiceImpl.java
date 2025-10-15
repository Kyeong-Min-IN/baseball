package com.baseball.game.service;

import com.baseball.game.dto.MemberDto;
import com.baseball.game.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by username: {}", username);
        // MemberMapper를 사용해 DB에서 사용자 정보 조회
        MemberDto member = memberMapper.member(username);

        if (member == null) {
            // 사용자가 없으면 예외 발생
            logger.warn("User not found with id: {}", username);
            throw new UsernameNotFoundException("User not found with id: " + username);
        }

        logger.debug("User found: {} (password length: {})", member.getId(), member.getPw() != null ? member.getPw().length() : 0);
        // Spring Security가 사용하는 UserDetails 객체로 변환하여 반환
        return new User(member.getId(), member.getPw(), Collections.emptyList());
    }
}
