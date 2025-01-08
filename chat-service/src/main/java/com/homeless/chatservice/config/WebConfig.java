package com.homeless.chatservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE","PATCH", "OPTIONS") // 허용할 HTTP 메서드
                        .allowedHeaders("Authorization", "Content-Type") // 허용할 요청 헤더
                        .allowCredentials(true);
            }
        };
    }
}