package com.homeless.chatservice.controller;


import com.homeless.chatservice.config.RabbitConfig;
import com.homeless.chatservice.dto.ChatMessageRequest;
import com.homeless.chatservice.service.StompMessageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final RabbitMessagingTemplate messagingTemplate;
    private final StompMessageService stompMessageService;
    private final RabbitConfig rabbitConfig;
    private final RabbitTemplate rabbitTemplate;

    @MessageMapping("chats.ch.{channelId}") // 웹소켓을 통해 들어오는 메시지의 목적지를 정함.
    @Operation(summary = "메시지 전송", description = "메시지를 전송합니다.")
    public void sendMessage(
            @DestinationVariable  String channelId,
            @Valid @Payload ChatMessageRequest chatMessageRequest) { // @DestinationVariable로 url의 동적 부분을 파라미터로 받는다.
        log.info("channelId: {}, chatMessage: {}", channelId, chatMessageRequest);

        // 메시지 저장
        stompMessageService.sendMessage(chatMessageRequest);
        rabbitConfig.sendMessageToQueue("/topic/chats.ch." + channelId, chatMessageRequest, rabbitTemplate);
        // 전송 후 클라이언트에게 메시지 응답
        messagingTemplate.convertAndSend("/topic/chats.ch." + channelId, chatMessageRequest);
    }

    // WebSocket 예외 처리
    @MessageExceptionHandler
    public void handleMessageException(RuntimeException e) {
        log.error("WebSocket error: {}", e.getMessage(), e);
        // 클라이언트에게 에러 메시지 전송
        messagingTemplate.convertAndSend("/topic/errors", "Error: " + e.getMessage());
    }
}
