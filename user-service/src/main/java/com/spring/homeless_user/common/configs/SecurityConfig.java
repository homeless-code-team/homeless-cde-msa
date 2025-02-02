package com.spring.homeless_user.common.configs;

import com.spring.homeless_user.common.auth.JwtAuthFilter;
import com.spring.homeless_user.common.dto.ErrorEntryPoint;
import com.spring.homeless_user.common.utill.SecurityPropertiesUtil;
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
import jakarta.servlet.http.HttpServletResponse;
import com.spring.homeless_user.common.auth.CustomOAuth2UserService;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    private final ErrorEntryPoint errorEntryPoint;
    private final JwtAuthFilter jwtAuthFilter;
    private final SecurityPropertiesUtil securityPropertiesUtil;
    private final CustomOAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("SecurityFilterChain 설정 시작");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> {
                    log.info("HTTP 요청 권한 설정");

                    // 허용된 경로 확인 (디버깅)
                    securityPropertiesUtil.getExcludedPaths().forEach(path -> log.info("허용된 경로: {}", path));

                    // ✅ OAuth2 리디렉트 경로 허용
                    auth.requestMatchers("/login/oauth2/code/**").permitAll();
                    auth.requestMatchers("/oauth2/**", "/login/**", "/api/v1/users/**").permitAll();

                    auth.anyRequest().authenticated();
                })
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> {
                            endpoint.baseUri("/oauth2/authorization");
                            DefaultOAuth2AuthorizationRequestResolver resolver =
                                    new DefaultOAuth2AuthorizationRequestResolver(
                                            clientRegistrationRepository,
                                            "/oauth2/authorization"
                                    );
                            endpoint.authorizationRequestResolver(resolver);
                        })
                        .redirectionEndpoint(endpoint ->
                                endpoint.baseUri("/login/oauth2/code/{registrationId}") // ✅ 기본 형식 유지
                        )
                        .userInfoEndpoint(endpoint ->
                                endpoint.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\": \"" + exception.getMessage() + "\"}");
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(errorEntryPoint));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8181"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
