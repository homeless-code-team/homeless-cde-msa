package com.playdata.homelesscode.common.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN) // HTTP 403 Forbidden
public class CustomThrowException extends RuntimeException {
    public CustomThrowException(String message) {
        super(message);
    }
}
