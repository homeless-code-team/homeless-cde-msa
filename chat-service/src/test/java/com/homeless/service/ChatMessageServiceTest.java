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
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Transactional
public class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatHttpService chatHttpService;

    @Test
    @Rollback
    public void 메시지생성테스트() {
        ChatMessageCreateCommand command = new ChatMessageCreateCommand("server-id", "channel-id","guest123","안녕" ,"user1");
        ChatMessage savedMessage = ChatMessage.builder()
                .id("testChatId")
                .channelId("test-channel")
                .content("Hello")
                .writer("user1")
                .timestamp(System.currentTimeMillis())
                .build();

        //when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

        String chatId = chatHttpService.createChatMessage(command);

        assertNotNull(chatId);
        assertEquals("testChatId", chatId);
    }

    @Test
    @Rollback
    void 메시지삭제테스트() {
        // given
        ChatMessageCreateCommand command = new ChatMessageCreateCommand("server-id", "channel-id","guest123","안녕" ,"user1");
        ChatMessage savedMessage = ChatMessage.builder()
                .id("123")
                .channelId("test-channel")
                .content("Hello")
                .writer("user1")
                .timestamp(System.currentTimeMillis())
                .build();
        // when
        String chatId = chatHttpService.createChatMessage(command);
        chatHttpService.deleteMessage(chatId);
        // then
        assertNull(chatId);
    }
}
