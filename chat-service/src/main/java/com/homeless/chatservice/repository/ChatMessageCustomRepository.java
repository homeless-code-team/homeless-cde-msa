package com.homeless.chatservice.repository;

import com.homeless.chatservice.entity.ChatMessage;

public interface ChatMessageCustomRepository {
    void updateContent(String id, String content);
}
