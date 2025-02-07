package com.spring.homeless_user.common.auth;

import com.spring.homeless_user.user.entity.Provider;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
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
        try {
            // OAuth2User에서 필요한 정보 추출
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            String registrationId = ((OAuth2AuthenticationToken) authentication)
                    .getAuthorizedClientRegistrationId();
            Provider provider = Provider.fromRegistrationId(registrationId);

            // 사용자 조회
            User user = userRepository.findByEmailAndProvider(email, registrationId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Cannot find user with email: " + email + " and provider: " + provider));

            // JWT 토큰 생성
            String accessToken = jwtTokenProvider.accessToken(
                    user.getEmail(),
                    user.getId(),
                    user.getNickname()
            );
            String refreshToken = jwtTokenProvider.refreshToken(
                    user.getEmail(),
                    user.getId()
            );

            // Redis에 refresh token 저장 (14일)
            loginTemplate.opsForValue().set(
                    user.getEmail(),
                    refreshToken,
                    14,
                    TimeUnit.DAYS
            );

            // 프론트엔드로 리다이렉트 (토큰 포함)
            String targetUrl = UriComponentsBuilder.fromUriString("https://homelesscode.shop/oauth/callback")
                    .queryParam("token", accessToken)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception ex) {
            log.error("OAuth2 로그인 성공 처리 중 오류 발생", ex);
            response.sendRedirect("https://homelesscode.shop/#/");
        }

    }
}
