package com.spring.homeless_user.common.utill;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    // secretKey를 안전하게 생성
    public JwtUtil(@Value("${jwt.secretKey}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 클레임 추출
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 안전한 키 사용
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "JWT token is expired");
        } catch (Exception e) {
            log.error("JWT parsing error: {}", e.getMessage());
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }

    // JWT에서 Email 추출
    public String getEmailFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token); // 클레임 추출
            return claims.getSubject(); // Subject를 이메일로 사용
        } catch (Exception e) {
            log.error("Error extracting email from token: {}", e.getMessage());
            throw new RuntimeException("Error extracting email from token", e);
        }
    }

    // 만료 시간 확인
    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // 만료된 토큰
        } catch (Exception e) {
            log.error("Error while checking token expiration: {}", e.getMessage());
            throw new RuntimeException("Error checking token expiration", e);
        }
    }
}