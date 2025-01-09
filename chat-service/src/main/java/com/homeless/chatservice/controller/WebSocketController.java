package com.homeless.chatservice.controller;

import com.homeless.chatservice.dto.ChatMessageRequest;
import com.homeless.chatservice.service.StompMessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    private final StompMessageService messageService;

    // 채팅 메시지 수신 및 저장
    @MessageMapping("chat.message.{channelId}.send") // 웹소켓을 통해 들어오는 메시지의 목적지를 정함.
    @Operation(summary = "메시지 전송", description = "메시지를 전송합니다.")
    public void sendMessage(@DestinationVariable String channelId, @Payload ChatMessageRequest chatMessage) { // @DestinationVariable로 url의 동적 부분을 파라미터로 받는다.
        log.info("roomId: {}, chatMessage: {}", channelId, chatMessage);
        // 메시지 저장
        messageService.sendMessage(chatMessage);

    }
}
