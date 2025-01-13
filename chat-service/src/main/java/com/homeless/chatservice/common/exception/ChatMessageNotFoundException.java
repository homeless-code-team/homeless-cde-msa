package com.homeless.chatservice.common.exception;

public class ChatMessageNotFoundException extends RuntimeException {
    public ChatMessageNotFoundException(String message) {
        super(message);
    }
}