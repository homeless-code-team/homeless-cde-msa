package com.homeless.chatservice.repository;


import com.homeless.chatservice.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String>, ChatMessageCustomRepository {

    Optional<ChatMessage> findById(String id); //

    void deleteChatMessageByChannelId(String channelId);

    Page<ChatMessage> findByChannelIdOrderByTimestampDesc(String channelId, Pageable pageable);

    Page<ChatMessage> findByChannelIdAndContentContainingOrderByTimestampDesc(String channelId, String content, Pageable pageable);

    Page<ChatMessage> findByChannelIdAndWriterContainingOrderByTimestampDesc(String channelId, String keyword, Pageable pageable);

    List<ChatMessage> findByChannelIdAndFileUrlIsNotNull(String channelId);


}