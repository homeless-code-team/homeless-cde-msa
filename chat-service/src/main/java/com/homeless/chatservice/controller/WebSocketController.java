package com.homeless.chatservice.controller;


import com.homeless.chatservice.config.RabbitConfig;
import com.homeless.chatservice.entity.ChatMessage;
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
    // 전송탬플릿
    private final RabbitTemplate rabbitTemplate;
    // 메시징 추상화 템플릿
    private final RabbitMessagingTemplate messagingTemplate;

    private final StompMessageService messageService;
    private final RabbitConfig rabbitConfig;

    @Value("${rabbitmq.chat-exchange.name}")
    private String CHAT_EXCHANGE_NAME;


    @MessageMapping("/api/v1/chats.{serverId}.{channelId}") // 웹소켓을 통해 들어오는 메시지의 목적지를 정함.
    @Operation(summary = "메시지 전송", description = "메시지를 전송합니다.")
    public void sendMessage(
            @DestinationVariable  String serverId,
            @DestinationVariable  String channelId,
            @Valid @Payload ChatMessage chatMessage) { // @DestinationVariable로 url의 동적 부분을 파라미터로 받는다.

        log.info("serverId: {},chanelId: {}, chatMessage: {}",serverId, channelId, chatMessage);

        //동적 라우팅키 생성
        String dynamicRoutingKey = "chat." + serverId + "." + channelId;
        // 메시지 저장
        messageService.sendMessage(chatMessage,serverId, channelId);
        // 메시지를 RabbitMQ에 전송
        rabbitConfig.sendMessageToQueue(dynamicRoutingKey, chatMessage, rabbitTemplate);

        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, dynamicRoutingKey, chatMessage);

        // 전송 후 클라이언트에게 메시지 응답
        messagingTemplate.convertAndSend("/topic/" + serverId + "." + channelId, chatMessage);
    }





    // WebSocket 예외 처리
    // WebSocket 예외 처리
    @MessageExceptionHandler
    public void handleMessageException(RuntimeException e) {
        log.error("WebSocket error: {}", e.getMessage(), e);
        // 클라이언트에게 에러 메시지 전송
        messagingTemplate.convertAndSend("/topic/errors", "Error: " + e.getMessage());
    }
}
