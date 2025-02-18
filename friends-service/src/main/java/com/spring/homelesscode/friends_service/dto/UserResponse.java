package com.spring.homelesscode.friends_service.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String receiver;
    private Boolean hasRefreshToken;

    // Getters and Setters
}

