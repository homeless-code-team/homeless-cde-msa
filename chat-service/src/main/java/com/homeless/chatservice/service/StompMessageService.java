package com.homeless.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeless.chatservice.common.config.RabbitConfig;
import com.homeless.chatservice.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class StompMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;  // WebSocket 메시지 전송용 추가
    private final RabbitConfig rabbitConfig;
    private final RabbitAdmin rabbitAdmin;
    private final Map<String, SimpleMessageListenerContainer> channelListeners = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${rabbitmq.chat-exchange.name}")
    private String CHAT_EXCHANGE_NAME;

    // 메시지를 RabbitMQ로 전달하는 메서드
    // Exchange의 이름과 라우팅 키를 조합하여 메시지를 목적지로 보낸다.
    @Transactional
    public void sendMessageFromRabbitMQ(MessageDto message) {
        // 1. 메시지 내용 해시 값 생성
        String messageContentHash = generateMessageHash(message.getContent());

        // 2. 중복 메시지 여부 확인
        boolean isDuplicate = isDuplicateMessage(message.getChannelId(), messageContentHash);
        if (isDuplicate) {
            return;
        }
        // 3. 메시지 전송
        String routingKey = "chat.channel." + message.getChannelId();;

        rabbitTemplate.convertAndSend(
                CHAT_EXCHANGE_NAME,
                routingKey,
                message
        );
    }


    // 중복 메시지 여부를 체크하는 메서드
// 중복 메시지 여부를 체크하는 메서드
    public boolean isDuplicateMessage(String channelId, String messageContentHash) {
        String redisKey = "chat:channel:" + channelId + ":messages"; // 채널별로 메시지를 구분하기 위해 키에 채널 ID 포함

        // Redis에서 최근 메시지들의 해시 목록을 가져옴
        String existingMessageHash = redisTemplate.opsForValue().get(redisKey);

        // 중복 메시지가 없으면, 현재 메시지의 해시를 저장
        if (existingMessageHash == null || !existingMessageHash.equals(messageContentHash)) {
            // 메시지 해시값을 Redis에 저장하고 TTL(시간 제한)을 설정하여 일정 시간 후 만료되도록 함
            redisTemplate.opsForValue().set(redisKey, messageContentHash, Duration.ofMinutes(5)); // 5분 동안 유효
            return false; // 중복되지 않음
        }

        // 중복 메시지인 경우 true 반환
        return true;
    }


    // 메시지 내용 해시값 생성
    private String generateMessageHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    // 바이트 배열을 16진수 문자열로 변환
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }


    private void createChannelListener(String channelId) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitConfig.connectionFactory());
        container.setQueueNames("chat.channel." + channelId);

        container.setMessageListener((message) -> {
            try {
                // RabbitTemplate의 메시지 컨버터를 사용하여 변환
                String jsonMessage = new String(message.getBody());
                ObjectMapper objectMapper = new ObjectMapper();
                MessageDto chatMessage = objectMapper.readValue(jsonMessage, MessageDto.class);

                messagingTemplate.convertAndSend(
                        "/exchange/chat.exchange/chat.channel." + channelId,
                        chatMessage
                );
            } catch (Exception e) {
                log.error("Error processing message for channel {}", channelId, e);
            }
        });

        container.start();
        channelListeners.put(channelId, container);
    }

    private void cleanupChannelResources(String channelId) {
        try {
            SimpleMessageListenerContainer container = channelListeners.remove(channelId);
            if (container != null) {
                container.stop();
            }
            rabbitAdmin.deleteQueue("chat.channel." + channelId);
        } catch (Exception e) {
            log.error("Error during channel cleanup: {}", channelId, e);
        }
    }

    private void sendSystemMessage(String channelId, String content) {
        MessageDto systemMessage = MessageDto.builder()
                .channelId(channelId)
                .content(content)
                .build();

        sendMessageFromRabbitMQ(systemMessage);
    }



    public void removeChannel(String channelId) {
        log.info("Removing channel: {}", channelId);

        // 리스너 정리
        SimpleMessageListenerContainer container = channelListeners.remove(channelId);
        if (container != null) {
            container.stop();
        }

        // 큐 삭제
        rabbitAdmin.deleteQueue("chat.channel." + channelId);

        // 시스템 메시지 발송
        sendSystemMessage(channelId, "채널이 삭제되었습니다.");
    }


    private String generateChannelId() {
        return UUID.randomUUID().toString();
    }

}