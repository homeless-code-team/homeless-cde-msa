package com.homeless.chatservice.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.addAllowedOrigin("https://jiangxy.github.io"); // 허용할 도메인
                    config.addAllowedOrigin("http://localhost:3000");
                    config.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
                    config.addAllowedHeader("*"); // 모든 헤더 허용
                    config.addAllowedHeader("Authorization"); // Authorization 헤더 허용

                    return config;
                })) // CORS 설정을 이곳에서 처리
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless 세션
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/api/chats/**").authenticated() // 인증된 사용자만 접근 가능
                                .requestMatchers("/api/public/**").permitAll() // 공개 경로
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll() // 디버깅 툴 경로 허용
                                .requestMatchers("https://jiangxy.github.io/**").permitAll() // 해당 도메인에서 오는 요청은 인증 없이 접근 가능
                                .requestMatchers("https://localhost:3000").permitAll()
                                .requestMatchers("/ws/**").permitAll() // WebSocket 경로 허용
                                .requestMatchers("https://jiangxy.github.io/**").permitAll() // 해당 도메인에서 오는 요청은 인증 없이 접근 가능
                                .anyRequest().denyAll() // 그 외 경로는 차단
                )
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint((request, response, authException) -> {
                                    log.error("Unauthorized request: {} from SecurityConfig", authException.getMessage());
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized:in SecurityConfig");
                                })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가

        return http.build();
    }
}
