package com.homeless.chatservice.service;

import com.homeless.chatservice.dto.ChatMessageRequest;
import com.homeless.chatservice.entity.ChatMessage;
import com.homeless.chatservice.dto.ChatMessageCreateCommand;
import com.homeless.chatservice.dto.ChatMessageResponse;
import com.homeless.chatservice.repository.ChatMessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatHttpService {

    private final ChatMessageRepository chatMessageRepository;
    public String createChatMessage(ChatMessageCreateCommand command) {
        // 채팅 메시지 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .serverId(command.serverId())
                .channelId(command.channelId())
                .writer(command.writer())
                .content(command.content())
                .timestamp(System.currentTimeMillis())
                .email(command.email())
                .build();
        System.out.println("Email: " + command.email());

        // MongoDB에 저장
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return savedMessage.getId();  // 저장된 메시지의 id 반환
    }

    public List<ChatMessageResponse> getMessagesByChannel(String channelId) {
        // 특정 채팅방의 메시지 조회
        return chatMessageRepository.findByChannelId(channelId).stream()
                .map(msg -> new ChatMessageResponse(
                        msg.getId(),
                        msg.getEmail(),
                        msg.getContent(),
                        msg.getWriter(),
                        msg.getTimestamp()
                ))
                .toList();
    }


    public void deleteMessage(String chatId) {
        chatMessageRepository.deleteById(chatId);
    }

    public void updateMessage(String chatId, ChatMessageRequest reqMessage) {
        ChatMessage chatMessage = chatMessageRepository.findById(chatId).orElseThrow();

        if (!chatMessage.getContent().equals(reqMessage.content())) {
            chatMessage.setContent(reqMessage.content());
            chatMessageRepository.save(chatMessage); // 변경된 내용만 저장
        }
    }

    public Optional<ChatMessage> getChatMessage(String chatId) {
        return chatMessageRepository.findById(chatId);
    }

    @Transactional
    public void deleteChatMessageByChannelId(String channelId){
        chatMessageRepository.deleteChatMessageByChannelId(channelId);
    }
}
