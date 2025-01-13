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
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractJwt(final StompHeaderAccessor accessor) {
        return accessor.getFirstNativeHeader("Authorization");
    }

    // jwt 인증
    public void validateToken(final String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(cleanToken);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "JWT 토큰이 잘못되었습니다.");
        }
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