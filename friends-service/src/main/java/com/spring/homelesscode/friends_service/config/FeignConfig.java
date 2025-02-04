package com.spring.homelesscode.friends_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@Slf4j
public class FeignConfig {


    private String getAuthToken() {
        // 인증 토큰 가져오는 로직 (예: SecurityContext에서 읽기)
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }
}
