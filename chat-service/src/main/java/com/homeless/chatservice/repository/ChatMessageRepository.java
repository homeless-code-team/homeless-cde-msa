package com.homeless.chatservice.repository;



import com.homeless.chatservice.entity.ChatMessage;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String>,ChatMessageCustomRepository {

    Optional<ChatMessage> findById(ObjectId chatId); //
    void deleteChatMessageByChannelId(String channelId);

    Page<ChatMessage> findByChannelIdOrderByTimestampDesc(String channelId, Pageable pageable);

    Page<ChatMessage> findByChannelIdAndContentContainingOrderByTimestampDesc(String channelId, String content, Pageable pageable);

    Page<ChatMessage> findByChannelIdAndWriterContainingOrderByTimestampDesc(String channelId, String keyword, Pageable pageable);

    Page<ChatMessage> findByChannelIdAndIdLessThanOrderByTimestampDesc(String channelId, ObjectId objectId, Pageable pageable);

}