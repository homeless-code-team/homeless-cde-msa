package com.spring.homeless_user.common.configs;

import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
@Slf4j
@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public CustomOAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        try {
            // 사용자 정보 가져오기
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String id = UUID.randomUUID().toString();
            String nickname = name != null ? name : "User-" + UUID.randomUUID().toString().substring(0, 8);

            log.info("email:{}, name:{}, id:{}", email, name, id);
            // 사용자 정보 저장 또는 업데이트
            userService.saveOrUpdateUser(email, name, id, nickname);

            String refreshToken = jwtTokenProvider.refreshToken(email, id);
            String accessToken = jwtTokenProvider.accessToken(email, id, nickname);

            // CORS 헤더 설정
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

            // JWT 토큰 응답
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format(
                "{\"token\": \"%s\", \"refreshToken\": \"%s\"}",
                accessToken,
                refreshToken
            ));
            response.getWriter().flush();

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            response.getWriter().flush();
        }
    }
}
