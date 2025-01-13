package com.homeless.chatservice.common.interceptor;

import com.homeless.chatservice.common.auth.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public JwtAuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("헤더(토큰) 없음");
            return false;
        }

        try {
            jwtUtils.validateToken(authorizationHeader);
            String email = jwtUtils.getEmailFromToken(authorizationHeader);
            request.setAttribute("userEmail", email); // 사용자 정보 저장
            return true;
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid token: " + e.getMessage());
            return false;
        }
    }
}
