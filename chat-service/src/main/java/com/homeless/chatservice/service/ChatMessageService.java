package com.homeless.chatservice.service;


import com.homeless.chatservice.common.entity.ChatMessage;
import com.homeless.chatservice.dto.ChatMessageCreateCommand;

import com.homeless.chatservice.dto.ChatMessageResponse;
import com.homeless.chatservice.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public String createChatMessage(ChatMessageCreateCommand command) {
        // 채팅 메시지 생성
        ChatMessage chatMessage = ChatMessage.builder()
            .serverId(command.serverId())
                .channelId(command.channelId())
                .writer(command.writer())
                .content(command.content())
                .timestamp(System.currentTimeMillis())
                .build();

        // MongoDB에 저장
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return savedMessage.getId();  // 저장된 메시지의 id 반환
    }


    public List<ChatMessageResponse> getMessagesByChannel(Long channelId) {
        // 특정 채팅방의 메시지 조회
        return chatMessageRepository.findByChannelId(channelId).stream()
            .map(msg -> new ChatMessageResponse(
                msg.getId(),
                msg.getContent(),
                msg.getWriter(),
                msg.getTimestamp()
            ))
            .toList();
    }
}
