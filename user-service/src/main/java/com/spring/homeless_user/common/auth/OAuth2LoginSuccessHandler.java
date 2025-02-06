package com.spring.homeless_user.common.auth;

import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> loginTemplate;

    public OAuth2LoginSuccessHandler(JwtTokenProvider jwtTokenProvider,
                                     UserRepository userRepository,
                                     @Qualifier("login") RedisTemplate<String, String> loginTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.loginTemplate = loginTemplate;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName(); // OAuth 로그인한 사용자 이메일
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            response.sendRedirect("https://homelesscode.shop/login?error=email-exists");
            return;
        }

        User user = userOpt.get();
        String jwtToken = jwtTokenProvider.accessToken(user.getEmail(), user.getId(), user.getNickname());
        String refreshToken = jwtTokenProvider.refreshToken(user.getEmail(), user.getId());

        // 🔹 Redis에 refresh token 저장
        loginTemplate.opsForValue().set(user.getEmail(),refreshToken, 14, TimeUnit.DAYS);

        // 🔥 클라이언트로 리디렉트할 URL 생성 (callback 페이지로 전달)
        String redirectUrl = UriComponentsBuilder.fromUriString("https://homelesscode.shop/oauth/callback")
                .queryParam("token", jwtToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
