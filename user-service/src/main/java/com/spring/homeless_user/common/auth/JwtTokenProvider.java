package com.spring.homeless_user.common.auth;


import com.spring.homeless_user.user.entity.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Slf4j
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


    // JWT AccessToken 생성
    public String accessToken(String email, String  id, String nickname) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(email)
                .claim("nickname", nickname)
                .claim("user_id",id)
                .claim("role", Role.USER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // JWT RefreshToken 생성
    public String refreshToken(String email, String  id) {
        return Jwts.builder()
                .setSubject(email)
                .claim("user_id",id)
                .claim("role", Role.USER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeRt))
                .signWith(SignatureAlgorithm.HS256, secretKeyRt)
                .compact();
    }
    // email token
    public String emailToken(String email) {
        // 6자리 짧은 토큰 생성
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        return token;
    }
}