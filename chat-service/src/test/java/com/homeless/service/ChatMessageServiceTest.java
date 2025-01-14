package com.homeless.service;

import com.homeless.chatservice.ChatServiceApplication;
import com.homeless.chatservice.dto.ChatMessageCreateCommand;
import com.homeless.chatservice.entity.ChatMessage;
import com.homeless.chatservice.repository.ChatMessageRepository;
import com.homeless.chatservice.service.ChatHttpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ChatServiceApplication.class) // 실제 데이터베이스를 사용하는 테스트
@Transactional  // 테스트 완료 후 롤백
public class ChatMessageServiceTest {
    @Autowired
    private ChatMessageRepository chatMessageRepository;  // 실제 레포지토리 사용
    @Autowired
    private ChatHttpService chatHttpService;  // 실제 서비스 사용



    @Test
    @Rollback
    void 통합_메시지_생성_테스트() {
        // given
        ChatMessageCreateCommand command = new ChatMessageCreateCommand("server-id", "channel-id", "guest123@1.com", "안녕", "geustUser");

        // when
        String chatId = chatHttpService.createChatMessage(command);

        // then
        assertNotNull(chatId);
        assertTrue(chatMessageRepository.existsById(chatId));  // DB에 저장된 데이터가 있는지 확인
    }

    @Test
    @Rollback
    void 통합_메시지_삭제_테스트() {
        // given
        ChatMessageCreateCommand command = new ChatMessageCreateCommand("server-id", "channel-id", "guest123", "안녕", "user1");
        String chatId = chatHttpService.createChatMessage(command);

        // when
        chatHttpService.deleteMessage(chatId);

        // then
        assertFalse(chatMessageRepository.existsById(chatId));  // DB에서 삭제된 데이터 확인
    }

    @Test
    @Rollback
    void 통합_메시지_수정_테스트() {
        // given
        ChatMessageCreateCommand command = new ChatMessageCreateCommand("server-id", "channel-id", "guest123", "안녕", "user1");
        String chatId = chatHttpService.createChatMessage(command);
        // when
        chatHttpService.updateMessage(chatId, "update");
        // then: DB에서 메시지가 업데이트되었는지 확인
        Optional<ChatMessage> message = chatMessageRepository.findById(chatId);

        // 메시지가 존재하는지 확인
        assertTrue(message.isPresent(), "Message should be present after update");

        // 업데이트된 메시지의 내용 확인
        ChatMessage updatedMessage = message.get();
        String updatedContent = updatedMessage.getContent();

        // 기대하는 업데이트된 내용과 비교
        assertEquals("update", updatedContent, "Message content should be updated");
    }
}
