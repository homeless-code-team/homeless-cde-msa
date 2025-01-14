package com.homeless.chatservice.repository;



import com.homeless.chatservice.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String>,ChatMessageCustomRepository {

    List<ChatMessage> findByChannelId(String channelId);  // 방 번호로 메시지 찾기
    void deleteChatMessageByChannelId(String channelId);

}