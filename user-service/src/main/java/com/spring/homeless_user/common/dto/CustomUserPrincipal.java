package com.spring.homeless_user.common.dto;

import lombok.*;

@Getter@Setter@ToString
public  class CustomUserPrincipal {
    private final String email;
    private final String userId;

    public CustomUserPrincipal(String email, String  userId) {
        this.email = email;
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public String  getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "CustomUserPrincipal{" +
                "email='" + email + '\'' +
                ", userId=" + userId +
                '}';
    }
}
