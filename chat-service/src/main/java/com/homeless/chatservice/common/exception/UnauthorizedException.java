package com.homeless.chatservice.exception;

public class UnauthorizedException extends RuntimeException {

    private String errorCode;
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

    // 오류 코드 가져오기
    public String getErrorCode() {
        return errorCode;
    }

    // 오류 메시지 가져오기
    public String getErrorMessage() {
        return errorMessage;
    }
}
