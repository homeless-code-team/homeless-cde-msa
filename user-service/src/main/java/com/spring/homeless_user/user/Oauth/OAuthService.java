package com.spring.homeless_user.user.Oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OAuthService {

    private final GoogleOAuthProperties googleOAuthProperties;
    private final GithubOAuthProperties githubOAuthProperties;

    @Autowired
    public OAuthService(GoogleOAuthProperties googleOAuthProperties, GithubOAuthProperties githubOAuthProperties) {
        this.googleOAuthProperties = googleOAuthProperties;
        this.githubOAuthProperties = githubOAuthProperties;
    }

    public String getGoogleRedirectUri() {
        return googleOAuthProperties.getRedirectUri();
    }

    public String getGithubRedirectUri() {
        return githubOAuthProperties.getRedirectUri();
    }
}
