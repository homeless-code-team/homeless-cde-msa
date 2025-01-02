package com.spring.homeless_user.common.auth;


import com.spring.homeless_user.common.dto.CustomUserPrincipal;
import com.spring.homeless_user.common.utill.JwtUtil;
import com.spring.homeless_user.common.utill.SecurityPropertiesUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> loginRedis;
    private final SecurityPropertiesUtil securityPropertiesUtil;

    public JwtAuthFilter(
            @Qualifier("login") RedisTemplate<String, String> loginRedis,
            JwtUtil jwtUtil,
            SecurityPropertiesUtil securityPropertiesUtil) {
        this.loginRedis = loginRedis;
        this.jwtUtil = jwtUtil;
        this.securityPropertiesUtil = securityPropertiesUtil;
    }

    //////////////////////////////////선언 종료 //////////////////////////////////////////////////////////////////


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("start");
        // 토큰 가져오기
        String token = request.getHeader("Authorization");

        // 토큰 없으면 컷
        if (token == null || token.trim().isEmpty()) {
            log.warn("Authorization header is missing or empty");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization header is missing or empty");
            return;
        }

        try {
            // Bearer 검증 및 공백 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim(); // "Bearer " 제거 후 공백 제거
            }

            // JWT 파싱 및 검증
            try {
                jwtUtil.extractAllClaims(token);
                String emailFromToken = jwtUtil.getEmailFromToken(token);
                log.info(emailFromToken);
            } catch (ExpiredJwtException e) {
                log.warn("Token has expired: {}", e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
                return;
            } catch (MalformedJwtException e) {
                log.warn("Malformed token: {}", e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Malformed token");
                return;
            } catch (IllegalArgumentException e) {
                log.error("Invalid token format: {}", e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid token format");
                return;
            } catch (Exception e) {
                log.error("Unexpected error during token validation: {}", e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error occurred");
                return;
            }

            jwtUtil.extractAllClaims(token);
            String email = jwtUtil.getEmailFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);

            log.info(email);
            log.info(String.valueOf(userId));
            // Redis 토큰 확인
            String redisToken = loginRedis.opsForValue().get(email);
            log.info(redisToken);
            if (redisToken == null) {
                log.warn("No token found in Redis for email: {}", email);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token not found in Redis");
                return;
            }

            if (!checkToken(token, redisToken)) {
                log.warn("Token mismatch for email: {}", email);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token mismatch");
                return;
            }

            // 인증 성공 후 인증 정보 설정
            log.info("Token validated successfully for email: {}", email);

            // 권한 정보 리스트 생성 (예: ROLE_USER)
            List<SimpleGrantedAuthority> authorityList = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            // Custom Principal 생성
            CustomUserPrincipal customPrincipal = new CustomUserPrincipal(email, userId);

            // 인증 객체 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    customPrincipal, // Principal (사용자 정보)
                    null, // Credentials (일반적으로 비밀번호는 null)
                    authorityList // Authorities (권한 리스트)
            );

// SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

// 다음 필터 호출
            filterChain.doFilter(request, response);


        } catch (IllegalArgumentException e) {
            log.error("Invalid token format: {}", e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid token format");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Unexpected error during token validation: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////서브 메서드 /////////////////////////////////////////////////////////////////////////////
    // 토큰 같은지 확인 로직
    private boolean checkToken(String reqToken, String redisToken) {
        return reqToken.equals(redisToken);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }

    //필터링 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestPath = request.getRequestURI();
        log.info("Request Path: {}", requestPath);
        log.info("Excluded Paths: {}", securityPropertiesUtil.getExcludedPaths());

        boolean flag = securityPropertiesUtil.getExcludedPaths()
                .stream()
                .anyMatch(excludedPath -> {
                    log.info("Comparing '{}' with '{}'", requestPath, excludedPath);
                    return requestPath.startsWith(excludedPath);
                });
        log.info("shouldNotFilter Flag: {}", flag);
        return flag;
    }

}