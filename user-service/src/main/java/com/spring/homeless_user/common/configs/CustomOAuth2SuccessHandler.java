package com.spring.homeless_user.common.configs;

import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
@Slf4j
@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final RedisTemplate<String, String> loginTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RedisTemplate<String, String> login;

    public CustomOAuth2SuccessHandler(@Qualifier("login")RedisTemplate<String, String> loginTemplate, JwtTokenProvider jwtTokenProvider, UserService userService, RedisTemplate<String, String> login) {
        this.loginTemplate = loginTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.login = login;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        try {
            log.info("OAuth 인증 성공. 사용자 속성: {}", oAuth2User.getAttributes());
            
            // 사용자 정보 가져오기
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String id = UUID.randomUUID().toString();
            String nickname = name != null ? name : "User-" + UUID.randomUUID().toString().substring(0, 8);

            log.info("처리된 사용자 정보 - email:{}, name:{}, id:{}, nickname:{}", 
                    email, name, id, nickname);

            // 사용자 정보 저장 또는 업데이트
            userService.saveOrUpdateUser(email, name, id, nickname);

            String refreshToken = jwtTokenProvider.refreshToken(email, id);
            String accessToken = jwtTokenProvider.accessToken(email, id, nickname);

            loginTemplate.opsForValue().set(email, refreshToken);

            // CORS 헤더 설정
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

            // JWT 토큰 응답
            response.setContentType("application/json;charset=UTF-8");
            String jsonResponse = String.format(
                "{\"status\":\"success\",\"token\":\"%s\",\"email\":\"%s\",\"nickname\":\"%s\"}",
                accessToken, email, nickname
            );
            
            log.info("인증 성공 응답 전송: {}", jsonResponse);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();

        } catch (Exception e) {
            log.error("OAuth 인증 성공 처리 중 오류 발생", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            response.getWriter().flush();
        }
    }
}
