package com.spring.homelesscode.friends_server.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@Slf4j
public class FeignConfig {

//    @Override
//    public void apply(RequestTemplate template) {
//        String token = getAuthToken(); // 인증 토큰 가져오기
//        log.info("token in feign client: {}", token);
//
//        if (token != null) {
//            template.header("Authorization", "Bearer " + token);
//        } else {
//            log.error("페인클라이언트 토큰 자동으로 안줌.");
//        }
//    }

    private String getAuthToken() {
        // 인증 토큰 가져오는 로직 (예: SecurityContext에서 읽기)
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }
}
