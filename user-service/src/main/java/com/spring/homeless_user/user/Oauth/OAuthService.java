package com.spring.homeless_user.user.Oauth;

import com.spring.homeless_user.user.dto.AccessTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

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



    public String getGithubRedirectUri() {
        return githubOAuthProperties.getRedirectUri();
    }
}
