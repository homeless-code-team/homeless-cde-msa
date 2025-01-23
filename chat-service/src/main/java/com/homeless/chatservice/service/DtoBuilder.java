package com.homeless.chatservice.service;

import com.homeless.chatservice.dto.ChatMessageCreateCommand;
import com.homeless.chatservice.dto.ChatMessageRequest;
import com.homeless.chatservice.dto.MessageDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class DtoBuilder {
    private final ChatHttpService chatHttpService;


    public MessageDto buildMessageDto(String chatId, String channelId, ChatMessageRequest chatReqDto) {
        return MessageDto.builder()
                .chatId(chatId)
                .channelId(channelId)
                .email(chatReqDto.email())
                .writer(chatReqDto.writer())
                .content(chatReqDto.content())
                .messageType(chatReqDto.messageType())
                .fileUrl(chatReqDto.fileUrl())
                .fileName(chatReqDto.fileName())
                .build();
    }

    public String saveChatMessage(String channelId, ChatMessageRequest chatReqDto) {
        ChatMessageCreateCommand chatMessageCreateCommand = ChatMessageCreateCommand.builder()
                .serverId(chatReqDto.serverId())
                .channelId(channelId)
                .email(chatReqDto.email())
                .writer(chatReqDto.writer())
                .content(chatReqDto.content())
                .messageType(chatReqDto.messageType())
                .fileUrl(chatReqDto.fileUrl())
                .fileName(chatReqDto.fileName())
                .build();
        return chatHttpService.createChatMessage(chatMessageCreateCommand);
    }
}
