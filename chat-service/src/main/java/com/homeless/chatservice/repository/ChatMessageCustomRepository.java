package com.homeless.chatservice.repository;


public interface ChatMessageCustomRepository {
    void updateContent(String id, String content);
}
