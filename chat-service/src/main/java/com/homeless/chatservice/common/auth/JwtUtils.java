package com.homeless.chatservice.common.auth;

import com.homeless.chatservice.common.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Key;

@Slf4j
@Component
public class JwtUtils {

    private final Key jwtSecretKey;

    public JwtUtils(@Value("${jwt.secret-key}") String secretKey) {
        log.info("secret key: {}", secretKey);
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("jwtSecret: {}", jwtSecretKey);
    }

    public String extractJwt(final StompHeaderAccessor accessor) {
        String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);  // "Bearer " 부분을 제거하고 토큰만 반환
        }
        return null;  // 올바른 형식의 토큰이 아니면 null 반환
    }

    // jwt 인증
    public String validateToken(final String token) {
        String tokenWithoutBearer;
        if (token != null && token.startsWith("Bearer ")) {
            tokenWithoutBearer = token.substring(7);
            log.info("tokenWithoutBearer: {}", tokenWithoutBearer);
        } else {
            log.warn("Invalid token");
            return null;
        }

        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(tokenWithoutBearer);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "JWT 토큰이 잘못되었습니다.");
        }

        return tokenWithoutBearer;
    }

    public String getEmailFromToken(String token) {
        Claims claims = getJwtParser()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // "sub" 필드에서 사용자 이름 추출
    }

    private JwtParser getJwtParser() {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey) // 서명 키 설정
                .build();
    }
}