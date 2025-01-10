package com.homeless.chatservice.exception;

public class ChatMessageNotFoundException extends RuntimeException {
    public ChatMessageNotFoundException(String message) {
        super(message);
    }
}