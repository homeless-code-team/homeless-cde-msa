package com.spring.homeless_user.user.dto;

import lombok.*;

@Getter@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String nickname;
    private Boolean hasRefreshToken;

    // Getters and Setters
}

