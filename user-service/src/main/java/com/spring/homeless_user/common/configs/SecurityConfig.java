package com.spring.homeless_user.common.configs;

import com.spring.homeless_user.common.auth.JwtAuthFilter;
import com.spring.homeless_user.common.dto.ErrorEntryPoint;
import com.spring.homeless_user.common.utill.SecurityPropertiesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ErrorEntryPoint errorEntryPoint;
    private final SecurityPropertiesUtil securityPropertiesUtil;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          ErrorEntryPoint errorEntryPoint,
                          SecurityPropertiesUtil securityPropertiesUtil) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.errorEntryPoint = errorEntryPoint;
        this.securityPropertiesUtil = securityPropertiesUtil;

    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
//                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {

                    // excludedPaths를 permitAll()로 설정
                    securityPropertiesUtil.getExcludedPaths().forEach(path ->
                            auth.requestMatchers(path).permitAll()
                    );
                    // 나머지 경로는 인증 필요
                    auth.anyRequest().authenticated();

                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(errorEntryPoint));

        return http.build();
    }
}
