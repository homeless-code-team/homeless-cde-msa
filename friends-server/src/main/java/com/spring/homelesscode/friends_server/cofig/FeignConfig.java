package com.spring.homelesscode.friends_server.cofig;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String token = getAuthToken(); // 인증 토큰 가져오기
        if (token != null) {
            template.header("Authorization", "Bearer " + token);
        }
    }

    private String getAuthToken() {
        // 인증 토큰 가져오는 로직 (예: SecurityContext에서 읽기)
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }
}
