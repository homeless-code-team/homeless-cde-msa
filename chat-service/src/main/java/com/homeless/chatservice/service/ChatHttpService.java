package com.homeless.chatservice.service;

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

    // 채팅 메시지 생성 및 저장
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

    // 메시지 리스트 조회
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

    // 메시지 삭제
    public void deleteMessage(String chatId) {
        chatMessageRepository.deleteById(chatId);
    }

    // 메시지 컨텐츠 업데이트
    public void updateMessage(String chatId, String reqMessage) {
        ChatMessage chatMessage = chatMessageRepository.findById(chatId).orElseThrow();

        if (!chatMessage.getContent().equals(reqMessage)) {
            chatMessageRepository.updateContent(chatId, reqMessage); // 변경된 내용만 저장
        }
    }

    // 메시지 조회
    public Optional<ChatMessage> getChatMessage(String chatId) {
        return chatMessageRepository.findById(chatId);
    }

    // 해당 채널의 모든 메시지 삭제
    @Transactional
    public void deleteChatMessageByChannelId(String channelId){
        chatMessageRepository.deleteChatMessageByChannelId(channelId);
    }
}
