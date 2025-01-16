package com.homeless.chatservice.repository;



import com.homeless.chatservice.entity.ChatMessage;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String>,ChatMessageCustomRepository {

    //Page<ChatMessage> findByChannelId(String channelId, Pageable pageable);  // 방 번호로 메시지 찾기
    Optional<ChatMessage> findById(ObjectId chatId); //
    void deleteChatMessageByChannelId(String channelId);
    Page<ChatMessage> findByChannelIdAndIdGreaterThanOrderByTimestampDesc(String channelId, ObjectId id, Pageable pageable);

    Page<ChatMessage> findByChannelIdOrderByTimestampDesc(String channelId, Pageable pageable);
}