package com.spring.homeless_user.common.utill;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {


    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @Value("${jwt.expirationRt}")
    private long expirationTimeRt;


//////////////////////클레임 추출/////////////////////////////////////////////////////////////////////////////////////////

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
            e.printStackTrace();
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "JWT token is expired");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("JWT parsing error: {}", e.getMessage());
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }

    // JWT에서 클레임 추출 (공통 메서드) 리프레쉬
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // JWT에서 Email 추출
    public String getEmailFromToken(String token) {
        Claims claims = extractAllClaims(token); // 클레임 추출
        return claims.getSubject(); // subject를 반환
    }


    // JWT에서 User ID 추출
    public Long getUserIdFromToken(String token) {
        return extractAllClaims(token).get("user_id", Long.class);
    }

    /////////////////유효성 검사////////////////////////////////////////////////////////////////////////////////////////////////
    // JWT 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 토큰 만료 처리
            return false;
        } catch (JwtException e) {
            // 일반적인 JWT 처리 오류
            return false;
        }
    }

    // JWT RefreshToken 유효성 검사
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 토큰 만료 처리
            return false;
        } catch (JwtException e) {
            // 일반적인 JWT 처리 오류
            return false;
        }
    }

    // 만료 시간 확인
    public boolean isTokenExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // 만료된 토큰
        } catch (Exception e) {
            log.error("Error while checking token expiration: {}", e.getMessage());
            throw new RuntimeException("Error checking token expiration", e);
        }
    }
}