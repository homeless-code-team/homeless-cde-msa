package com.homeless.chatservice.usecase;

import com.homeless.chatservice.dto.ChatMessageCreateCommand;
import org.springframework.stereotype.Component;

@Component
public interface ChatMessageCreateUseCase {
    // dto를 매개변수로 채팅 메세지 생성 매서드
    Long createChatMessage(ChatMessageCreateCommand command);
}
