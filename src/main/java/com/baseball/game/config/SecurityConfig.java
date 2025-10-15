package com.baseball.game.config;

import com.baseball.game.dto.MemberDto;
import com.baseball.game.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import static org.springframework.security.config.Customizer.withDefaults; // withDefaults 임포트
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper 주입

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService customUserDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // CORS 설정을 Bean으로 등록
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*", "Authorization"));
        configuration.setAllowCredentials(true); // ★★★ 이 부분이 true여야 합니다.

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정 적용
        return source;
    }

    // 로그인 성공 핸들러 (JSON 응답만 반환, 리다이렉션 방지)
    // MemberService를 파라미터로 받음
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(MemberService memberService) {
        return (request, response, authentication) -> {
            response.setStatus(200);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try {
                String username = authentication.getName();
                MemberDto member = memberService.getMember(username); // 사용자 정보 조회
                member.setPw(null); // 비밀번호는 응답에서 제외

                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("success", true);
                responseBody.put("message", "Login successful");
                responseBody.put("userInfo", member); // userInfo 포함

                response.getWriter().write(objectMapper.writeValueAsString(responseBody));
                response.flushBuffer(); // 응답을 즉시 커밋
            } catch (IOException e) { e.printStackTrace(); }
        };
    }

    // 로그인 실패 핸들러 (JSON 응답만 반환, 리다이렉션 방지)
    // MemberService를 파라미터로 받음 (일관성을 위해)
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler(MemberService memberService) {
        return (request, response, exception) -> {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try {
                response.getWriter().write("{\"success\": false, \"message\": \"Invalid credentials\"}");
                response.flushBuffer(); // 응답을 즉시 커밋
            } catch (IOException e) { e.printStackTrace(); }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MemberService memberService) throws Exception {
        http
            // 1. CORS 설정을 Security Filter Chain에 통합 (withDefaults 사용)
            .cors(withDefaults())
            // 2. CSRF 보호는 세션 방식에서 필요하지만, API 테스트 및 개발 편의를 위해 잠시 비활성화
            .csrf(csrf -> csrf.disable())
            
            .authorizeHttpRequests(authorize -> authorize
                // 특정 API 경로에 대한 OPTIONS 요청만 허용
                .antMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .antMatchers("/", "/index.html", "/static/**", "/favicon.ico", "/manifest.json", "/logo*.png").permitAll()
                .antMatchers(HttpMethod.POST, "/api/login", "/api/members/add", "/api/login/register").permitAll() // 회원가입 경로 추가
                .antMatchers("/api/kbo/**").permitAll() // KBO 통계 관련 API 모두 허용
                .antMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginProcessingUrl("/api/login")
                .successHandler(authenticationSuccessHandler(memberService)) // 빈으로 등록된 핸들러 사용
                .failureHandler(authenticationFailureHandler(memberService)) // 빈으로 등록된 핸들러 사용
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                    try {
                        response.getWriter().write("{\"message\": \"Logout successful\"}");
                    } catch (IOException e) { e.printStackTrace(); }
                })
                .deleteCookies("SESSION")
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin()
            );

        return http.build();
    }
}
