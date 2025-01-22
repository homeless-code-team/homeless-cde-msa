package com.homeless.chatservice.service;

import com.homeless.chatservice.common.config.AwsS3Config;
import com.homeless.chatservice.common.config.RabbitConfig;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;  // WebSocket 메시지 전송용 추가
    private final RabbitConfig rabbitConfig;
    private final RabbitAdmin rabbitAdmin;
    private final Map<String, SimpleMessageListenerContainer> channelListeners = new ConcurrentHashMap<>();
    private final AwsS3Config awsS3Config;

    @Value("${rabbitmq.chat-exchange.name}")
    private String CHAT_EXCHANGE_NAME;

    private final RedisTemplate<String, String> redisTemplate;




    @Transactional
    public String uploadFile(MultipartFile file) throws IOException {
        // 파일 이름을 UUID로 생성하여 고유하게 처리 (필요 시 UUID 대신 다른 방식 사용 가능)
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // 파일을 S3에 업로드하고 URL을 반환
        byte[] fileBytes = file.getBytes();

        return awsS3Config.uploadToS3Bucket(fileBytes, fileName); // 업로드된 파일의 URL 반환
    }

    // 파일 삭제 처리 메서드 (파일 URL을 이용해 삭제)
    public void deleteFile(String fileUrl) throws Exception {
        awsS3Config.deleteFromS3Bucket(fileUrl);
    }
}
