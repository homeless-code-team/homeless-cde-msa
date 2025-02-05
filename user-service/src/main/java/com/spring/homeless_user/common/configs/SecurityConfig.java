package com.spring.homeless_user.common.configs;

import com.spring.homeless_user.common.auth.OAuth2LoginSuccessHandler;
import com.spring.homeless_user.user.service.OAuth2UserServiceImpl;
import com.spring.homeless_user.common.utill.SecurityPropertiesUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2SuccessHandler;
    private final OAuth2UserServiceImpl oAuth2UserService;
    private final SecurityPropertiesUtil securityPropertiesUtil; // ✅ SecurityPropertiesUtil 추가

    public SecurityConfig(OAuth2LoginSuccessHandler oAuth2SuccessHandler,
                          OAuth2UserServiceImpl oAuth2UserService,
                          SecurityPropertiesUtil securityPropertiesUtil) {
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2UserService = oAuth2UserService;
        this.securityPropertiesUtil = securityPropertiesUtil; // ✅ 주입
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // YAML 설정과 통합
                .authorizeHttpRequests(auth -> {
                    // ✅ YAML에서 설정된 경로를 검증 없이 통과하도록 허용
                    securityPropertiesUtil.getExcludedPaths().forEach(path ->
                            auth.requestMatchers(path).permitAll()
                    );

                    auth.anyRequest().authenticated(); // 그 외 모든 요청은 인증 필요
                })
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService)) // ✅ OAuth2UserServiceImpl 사용
                        .successHandler(oAuth2SuccessHandler) // ✅ 로그인 성공 후 JWT 발급
                );

        return http.build();
    }
}
