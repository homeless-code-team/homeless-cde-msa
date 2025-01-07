package com.spring.homeless_user.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailCheckDto {
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "token is required")
    private String token;
}
