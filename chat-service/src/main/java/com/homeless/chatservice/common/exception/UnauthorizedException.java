package com.homeless.chatservice.common.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    // 오류 코드 가져오기
    private String errorCode;
    // 오류 메시지 가져오기
    private String errorMessage;

    // 생성자
    private UnauthorizedException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    // 팩토리 메서드
    public static UnauthorizedException of(String errorCode, String errorMessage) {
        return new UnauthorizedException(errorCode, errorMessage);
    }

}
