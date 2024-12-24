package com.spring.homeless_user.common.auth;



import com.spring.homeless_user.user.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private  Role role;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @Value("${jwt.expirationRt}")
    private long expirationTimeRt;


    // SecretKey 생성
    private SecretKey generateSecretKey(String key) {
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey accessSecretKey() {
        return generateSecretKey(secretKey);
    }

    private SecretKey refreshSecretKey() {
        return generateSecretKey(secretKeyRt);
    }

    // JWT AccessToken 생성
    public String accessToken(String email) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(email)
                .claim("email", email)
                .claim("role", Role.USER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(accessSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT RefreshToken 생성
    public String refreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .claim("role", Role.USER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeRt))
                .signWith(refreshSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT에서 Email 추출
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(accessSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // JWT 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(accessSecretKey())
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
                    .setSigningKey(refreshSecretKey())
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

}