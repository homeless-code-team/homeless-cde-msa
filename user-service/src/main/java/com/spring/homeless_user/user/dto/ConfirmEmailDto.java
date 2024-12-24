package com.spring.homeless_user.user.dto;

import lombok.Data;

@Data
public class ConfirmEmailDto {
    private String email;
    private String token;
}
