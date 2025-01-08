package com.homeless.chatservice.controller;


import com.homeless.chatservice.common.entity.ChatMessage;
import com.homeless.chatservice.service.ChatHttpService;
import com.homeless.chatservice.service.StompMessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    //    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMessagingTemplate messagingTemplate;

    private final StompMessageService messageService;
    private final ChatHttpService chatHttpService;

    // 채팅 메시지 수신 및 저장
    @MessageMapping("/api/v1/chats.{serverId}.{channelId}") // 웹소켓을 통해 들어오는 메시지의 목적지를 정함.
    @Operation(summary = "메시지 전송", description = "메시지를 전송합니다.")
    public void sendMessage(
            @DestinationVariable String serverId,
            @DestinationVariable String channelId,
            @Payload ChatMessage chatMessage) { // @DestinationVariable로 url의 동적 부분을 파라미터로 받는다.
        log.info("serverID: {},roomId: {}, chatMessage: {}",serverId, channelId, chatMessage);
        // 메시지 저장
        messageService.sendMessage(chatMessage);
        rabbitTemplate.convertAndSend("exchangeName", "routingKey", chatMessage);

    }



    // WebSocket 예외 처리
    @MessageExceptionHandler
    public void handleMessageException(RuntimeException e) {
        log.error("WebSocket error: {}", e.getMessage(), e);
        // 클라이언트에게 에러 메시지 전송 가능 (필요할 경우)
        messagingTemplate.convertAndSend("/topic/errors", e.getMessage());
    }
}
