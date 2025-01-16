package com.homeless.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeless.chatservice.common.config.RabbitConfig;
import com.homeless.chatservice.dto.CreateChannelRequest;
import com.homeless.chatservice.dto.JoinMessage;
import com.homeless.chatservice.dto.LeaveMessage;
import com.homeless.chatservice.entity.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
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

    @Value("${rabbitmq.chat-exchange.name}")
    private String CHAT_EXCHANGE_NAME;

    private final RedisTemplate<String, String> redisTemplate;

    // 1. 채널 관리 관련 메서드들
    public String createChannel(CreateChannelRequest request) {
        String channelId = generateChannelId(); // UUID 등을 사용하여 채널 ID 생성 (DB의 uuid 전략 사용해도 무방)

        try {
            // 큐 생성
            Queue queue = rabbitConfig.createChatQueue(channelId);
            rabbitAdmin.declareQueue(queue);

            // 바인딩 생성
            Binding binding = rabbitConfig.createChatChannelBinding(queue, channelId);
            rabbitAdmin.declareBinding(binding);

            // 리스너 생성 및 시작
            createChannelListener(channelId);

            log.info("Created channel: {}", channelId);
            return channelId;

        } catch (Exception e) {
            log.error("Failed to create channel: {}", channelId, e);
            cleanupChannelResources(channelId);
            throw new RuntimeException("Failed to create channel", e);
        }
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




    // 메시지를 RabbitMQ로 전달하는 메서드
    // Exchange의 이름과 라우팅 키를 조합하여 메시지를 목적지로 보낸다.
    @Transactional
    public void sendMessage(MessageDto message) {
        // 1. 메시지 내용 해시 값 생성
        String messageContentHash = generateMessageHash(message.getContent());

        // 2. 중복 메시지 여부 확인
        boolean isDuplicate = isDuplicateMessage(message.getChannelId(), messageContentHash);
        if (isDuplicate) {
            log.warn("Duplicate message detected. Message will not be sent.");
            return; // 중복 메시지일 경우 전송하지 않음
        }

        // 3. 메시지 전송
        String routingKey = "chat.channel." + message.getChannelId();
        log.info("Sending message to exchange: {}, routing key: {}", CHAT_EXCHANGE_NAME, routingKey);

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

    public void handleJoinChannel(String channelId, JoinMessage joinMessage) {
        /*
        // 채널이 존재하지 않으면 생성
        // 데이터베이스에서 채널 조회 후 없으면 새롭게 생성하는 로직 (추후에 DB와 연동해서 처리하면 됩니다.)
        if (!chatChannelRepository.existsById(channelId)) {
            createChannel(channelId);
        }
        */

        log.info("User {} joining channel {}", joinMessage.getUserId(), channelId);
        sendSystemMessage(channelId, joinMessage.getUserName() + "님이 입장하셨습니다.");

        // 채널 참여자 정보 저장
        // parameter: 채널아이디, 참여하려는 사용자 아이디, 입장/퇴장 flag
        // updateChannelParticipants(channelId, joinMessage.getUserId(), true);
    }

    // 사용자가 채널을 떠날 때 사용하는 메서드
    public void handleLeaveChannel(String channelId, LeaveMessage leaveMessage) {
        log.info("User {} leaving channel {}", leaveMessage.getUserId(), channelId);
        sendSystemMessage(channelId, leaveMessage.getUserId() + "님이 퇴장하셨습니다.");

        // 채널 참여자 정보 업데이트 (이것도 DB 연동 로직 나중에 추가하면 됩니다.)
        // 채널에다가 현재 참여중인 사용자의 정보를 업데이트 합니다.
        // parameter: 채널아이디, 참여하려는 사용자 아이디, 입장/퇴장 flag
        // updateChannelParticipants(channelId, leaveMessage.getUserId(), false);
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

        sendMessage(systemMessage);
    }


    private String generateChannelId() {
        return UUID.randomUUID().toString();
    }

}