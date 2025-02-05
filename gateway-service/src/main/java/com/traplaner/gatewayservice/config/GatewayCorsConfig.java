package com.traplaner.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GatewayCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://homelesscode.shop"); // 허용할 도메인
        config.addAllowedOrigin("https://homelesscode.shop"); // 허용할 도메인
        config.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        config.addAllowedHeader("*"); // 모든 헤더 허용
        config.addExposedHeader("Authorization"); // 클라이언트가 접근할 수 있는 헤더
        config.addExposedHeader("Set-Cookie");
        config.setAllowCredentials(true); // 쿠키 및 인증 허용
        config.setMaxAge(3600L); // Preflight 요청 캐싱 시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
