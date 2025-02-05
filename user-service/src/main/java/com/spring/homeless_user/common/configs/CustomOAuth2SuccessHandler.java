package com.spring.homeless_user.common.configs;

import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.user.service.UserService;
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

    public CustomOAuth2SuccessHandler(@Qualifier("login") RedisTemplate<String, String> loginTemplate,
                                      JwtTokenProvider jwtTokenProvider,
                                      UserService userService) {
        this.loginTemplate = loginTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String id = UUID.randomUUID().toString();
        String nickname = oAuth2User.getAttribute("name");

        log.info("OAuth2 로그인 성공 - email: {}, nickname: {}", email, nickname);
        // 회원가입 또는 로그인 처리
        userService.registerOrLoginOAuthUser(email, nickname);

        // JWT 발급
        String accessToken = jwtTokenProvider.accessToken(email, id, nickname);
        String refreshToken = jwtTokenProvider.refreshToken(email,id);

        loginTemplate.opsForValue().set(email, refreshToken);

        log.info("OAuth2 로그인 성공 - email: {}, JWT 발급 완료", email);

        // ✅ 새 창에서 기존 창으로 JWT 전송 & 창 닫기
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(
                "<script>" +
                        "window.opener.postMessage({ token: '" + accessToken + "' }, '*');" + // 기존 창으로 토큰 전달
                        "window.close();" + // 팝업 창 닫기
                        "</script>"
        );
    }

}