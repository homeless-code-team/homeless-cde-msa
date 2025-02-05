package com.spring.homeless_user.common.auth;

import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName(); // OAuth 로그인한 사용자 이메일
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

        // 🔹 JWT 발급
        String jwtToken = jwtTokenProvider.accessToken(user.getEmail(), user.getNickname(), user.getId());

        // 🔥 클라이언트로 리디렉트할 URL 생성
        String redirectUrl = UriComponentsBuilder.fromUriString("https://homelesscode.shop/callback")
                .queryParam("token", jwtToken)
                .build().toUriString();

        // 🔹 클라이언트로 리디렉트
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
