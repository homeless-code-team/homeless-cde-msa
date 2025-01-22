package com.spring.homelesscode.friends_server.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CustomUserPrincipal {
    private final String email;
    private final String userId;
    private final String nickname;

    public CustomUserPrincipal(String email, String userId, String nickname) {
        this.email = email;
        this.userId = userId;
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public String toString() {
        return "CustomUserPrincipal{" +
                "email='" + email + '\'' +
                ", userId=" + userId + ", nickname=" + nickname +
                '}';
    }
}
