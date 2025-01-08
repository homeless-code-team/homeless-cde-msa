package com.homeless.chatservice.service;

import com.homeless.chatservice.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class StompMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;  // WebSocket 메시지 전송용 추가

    @Value("${rabbitmq.chat-exchange.name}")
    private String CHAT_EXCHANGE_NAME;


    // 메시지를 RabbitMQ로 전달하는 메서드
    // Exchange의 이름과 라우팅 키를 조합하여 메시지를 목적지로 보낸다.
    public void sendMessage(ChatMessage message) {
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME,
                "chat.room." + message.getId(),
                message
        );
    }

    // RabbitMQ로부터 메시지를 수신하여 WebSocket 구독자들에게 전달
    @RabbitListener(queues = "${rabbitmq.chat-queue.name}")
    public void handleMessage(ChatMessage message) {
        log.info("Received message from queue: {}", message);
        // WebSocket 구독자들에게 메시지 전달
        messagingTemplate.convertAndSend("/topic/chat.room." + message.getId(), message);
    }

}