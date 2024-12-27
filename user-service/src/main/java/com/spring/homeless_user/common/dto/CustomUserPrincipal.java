package com.spring.homeless_user.common.dto;

import lombok.*;

@Getter@Setter@ToString
public  class CustomUserPrincipal {
    private final String email;
    private final Long userId;

    public CustomUserPrincipal(String email, Long userId) {
        this.email = email;
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public Long getUserId() {
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
