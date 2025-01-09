package com.homeless.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeless.chatservice.config.RabbitConfig;
import com.homeless.chatservice.dto.CreateChannelRequest;
import com.homeless.chatservice.dto.JoinMessage;
import com.homeless.chatservice.dto.LeaveMessage;
import com.homeless.chatservice.dto.MessageType;
import com.homeless.chatservice.entity.ChatMessage;
import com.homeless.chatservice.entity.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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
    public void sendMessage(MessageDto message) {
        String routingKey = "chat.channel." + message.getChannelId();
        log.info("Sending message to exchange: {}, routing key: {}",
                CHAT_EXCHANGE_NAME, routingKey);
        rabbitTemplate.convertAndSend(
                CHAT_EXCHANGE_NAME,
                routingKey,
                message
        );
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