package com.spring.homeless_user.common.auth;


import com.spring.homeless_user.common.dto.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 현재 요청 URI 가져오기
        String requestURI = request.getRequestURI();
        log.info("request uri: {}", requestURI);

        // OAuth2 경로는 필터를 건너뜀
        if (requestURI.startsWith("/oauth2/**") || requestURI.startsWith("/login/oauth2/**")) {
            log.info("Skipping JwtAuthFilter for URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 게이트웨이가 토큰 내에 클레임을 헤더에 담아서 보내준다.
        String userEmail = request.getHeader("X-User-Email");
        String userId = request.getHeader("X-User-Id");
        String nickname = request.getHeader("X-User-Nickname");

        log.info("userEmail: {}, userId: {}, nickname: {}", userEmail, userId, nickname);

        if (userEmail != null) {
            List<SimpleGrantedAuthority> authorityList = new ArrayList<>();

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    new CustomUserPrincipal(userEmail, userId, nickname), // 컨트롤러 등에서 활용할 유저 정보
                    "",
                    authorityList
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Authentication set for user: {}", userEmail);
        }

        filterChain.doFilter(request, response);
    }
}
