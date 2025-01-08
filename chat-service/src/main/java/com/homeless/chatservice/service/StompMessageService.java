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


    // 라우팅 키를 동적으로 처리하는 메서드로 수정
    public void sendMessage(ChatMessage message, String serverId, String channelId) {
        // 새로운 라우팅 키로 메시지를 전송
        String routingKey = "chat." + serverId + "." + channelId;
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, routingKey, message);
        log.info("Sent message to RabbitMQ with routingKey: {}", routingKey);
    }

    // RabbitMQ로부터 메시지를 수신하여 WebSocket 구독자들에게 전달
    @RabbitListener(queues = "${rabbitmq.chat-queue.name}")
    public void handleMessage(ChatMessage message) {
        log.info("Received message from queue: {}", message);
        // WebSocket 구독자들에게 메시지 전달
        // WebSocket 메시지를 "/topic/chat.${serverId}.${channelId}" 형식으로 전송
        String routingKey = "chat." + message.getServerId() + "." + message.getChannelId();  // 채팅방 식별자에 맞게
        messagingTemplate.convertAndSend("/topic/" + routingKey, message);
        log.info("Sent message to WebSocket: /topic/{}", routingKey);
    }
}
