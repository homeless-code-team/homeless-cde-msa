package com.homeless.service;


import com.homeless.chatservice.entity.ChatMessage;
import com.homeless.chatservice.dto.ChatMessageCreateCommand;
import com.homeless.chatservice.repository.ChatMessageRepository;
import com.homeless.chatservice.service.ChatHttpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatHttpService chatHttpService;

    @Test
    public void testCreateChatMessage() {
        ChatMessageCreateCommand command = new ChatMessageCreateCommand(1L, 1L,"안녕" ,"user1");
        ChatMessage savedMessage = ChatMessage.builder()
                .id("123")
                .channelId(1L)
                .content("Hello")
                .writer("user1")
                .timestamp(System.currentTimeMillis())
                .build();

        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

        String chatId = chatHttpService.createChatMessage(command);

        assertNotNull(chatId);
        assertEquals("123", chatId);
    }
}
