package com.spring.homeless_user.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class CommonResDto {
    private HttpStatus status;
    private String message;
    private Object data;
}
