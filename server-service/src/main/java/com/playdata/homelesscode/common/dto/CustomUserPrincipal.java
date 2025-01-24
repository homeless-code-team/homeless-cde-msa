package com.playdata.homelesscode.common.dto;

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


    public CustomUserPrincipal(String email, String userId, String nickName) {
        this.email = email;
        this.userId = userId;
        this.nickname = nickName;

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
                ", userId=" + userId +
                '}';
    }
}
