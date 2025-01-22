package com.spring.homeless_user.user.Oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class OAuthService {

    private final GoogleOAuthProperties googleOAuthProperties;
    private final GithubOAuthProperties githubOAuthProperties;

    @Autowired
    public OAuthService(GoogleOAuthProperties googleOAuthProperties, GithubOAuthProperties githubOAuthProperties) {
        this.googleOAuthProperties = googleOAuthProperties;
        this.githubOAuthProperties = githubOAuthProperties;
    }
    // Google 인증 URL 생성
    public String getGoogleAuthUrl() {
        return UriComponentsBuilder.fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleOAuthProperties.getClientId())
                .queryParam("redirect_uri", googleOAuthProperties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "email profile")
                .queryParam("access_type", "offline")
                .build().toUriString();
    }
    public String getGithubAuthUrl() {
        return UriComponentsBuilder.fromHttpUrl("https://github.com/login/oauth/authorize")
                .queryParam("client_id", githubOAuthProperties.getClientId())
                .queryParam("redirect_uri", githubOAuthProperties.getRedirectUri())
                .queryParam("scope", "read:user user:email") // 올바른 스코프
                .queryParam("state", "your_state_value")    // CSRF 방지를 위한 state 값
                .build().toUriString();
    }

}
