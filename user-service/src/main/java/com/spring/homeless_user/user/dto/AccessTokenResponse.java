package com.spring.homeless_user.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AccessTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;  // 액세스 토큰

    @JsonProperty("refresh_token")
    private String refreshToken; // 리프레시 토큰

    @JsonProperty("token_type")
    private String tokenType;    // 토큰 유형 (예: Bearer)

    @JsonProperty("expires_in")
    private int expiresIn;       // 액세스 토큰 유효 시간
}
