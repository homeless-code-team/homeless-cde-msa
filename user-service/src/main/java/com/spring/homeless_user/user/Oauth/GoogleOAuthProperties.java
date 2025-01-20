package com.spring.homeless_user.user.Oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "oauth.provider.google")
public class GoogleOAuthProperties {

    // Getter와 Setter 생성
    private String tokenUrl;
    private String userInfoUrl;
    private String clientId;
    private String clientSecret;
    private String redirectUri;

}
