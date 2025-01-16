package com.homeless.chatservice.repository;



import com.homeless.chatservice.entity.ChatMessage;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String>,ChatMessageCustomRepository {

    List<ChatMessage> findByChannelId(String channelId);  // 방 번호로 메시지 찾기
    Optional<ChatMessage> findById(ObjectId chatId); //
    void deleteChatMessageByChannelId(String channelId);

}