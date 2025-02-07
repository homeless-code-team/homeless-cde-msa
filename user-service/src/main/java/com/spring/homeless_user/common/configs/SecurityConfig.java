package com.spring.homeless_user.common.configs;

import com.spring.homeless_user.common.auth.JwtAuthFilter;
import com.spring.homeless_user.common.auth.OAuth2LoginSuccessHandler;
import com.spring.homeless_user.common.dto.ErrorEntryPoint;
import com.spring.homeless_user.user.service.OAuth2UserServiceImpl;
import com.spring.homeless_user.common.utill.SecurityPropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2SuccessHandler;
    private final OAuth2UserServiceImpl oAuth2UserService;
    private final SecurityPropertiesUtil securityPropertiesUtil; // ✅ SecurityPropertiesUtil 추가
    private final ErrorEntryPoint errorEntryPoint;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(OAuth2LoginSuccessHandler oAuth2SuccessHandler,
                          OAuth2UserServiceImpl oAuth2UserService,
                          SecurityPropertiesUtil securityPropertiesUtil, ErrorEntryPoint errorEntryPoint, JwtAuthFilter jwtAuthFilter) {
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2UserService = oAuth2UserService;
        this.securityPropertiesUtil = securityPropertiesUtil; // ✅ 주입
        this.errorEntryPoint = errorEntryPoint;
        this.jwtAuthFilter = jwtAuthFilter;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // ✅ YAML에서 설정된 경로를 인증 없이 허용
                    securityPropertiesUtil.getExcludedPaths().forEach(path ->
                            auth.requestMatchers(path).permitAll()
                    );

                    // ✅ OAuth2 로그인 관련 엔드포인트 인증 없이 허용
                    auth.requestMatchers("api/v1/oauth2/code/**", "login/oauth2/code/**").permitAll();

                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(errorEntryPoint);
                })
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> {
                            endpoint.baseUri("/api/v1/oauth2/authorization");
                            log.info("Authorization endpoint configured");
                        })
                        .redirectionEndpoint(endpoint -> {
                            endpoint.baseUri("/login/oauth2/code/*");
                            log.info("Redirection endpoint configured");
                        })
                        .userInfoEndpoint(userInfo -> {
                            log.info("Setting up user info endpoint");
                            userInfo.userService(oAuth2UserService);
                        })
                        .successHandler((request, response, authentication) -> {
                            log.info("OAuth2 success handler triggered");
                            oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);
                        })
                );

        return http.build();
    }

}
