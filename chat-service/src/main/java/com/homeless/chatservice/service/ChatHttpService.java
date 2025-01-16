package com.homeless.chatservice.service;

import com.homeless.chatservice.entity.ChatMessage;
import com.homeless.chatservice.dto.ChatMessageCreateCommand;
import com.homeless.chatservice.dto.ChatMessageResponse;
import com.homeless.chatservice.repository.ChatMessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatHttpService {

    private final ChatMessageRepository chatMessageRepository;
    private final MongoTemplate mongoTemplate;

    // 채팅 메시지 생성 및 저장
    public String createChatMessage(ChatMessageCreateCommand command) {
        // 채팅 메시지 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .serverId(command.serverId())
                .channelId(command.channelId())
                .writer(command.writer())
                .content(command.content())
                .email(command.email())
                .messageType(command.messageType())
                .timestamp(System.currentTimeMillis())
                .build();
        System.out.println("Email: " + command.email());

        // MongoDB에 저장
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return savedMessage.getId();  // 저장된 메시지의 id 반환
    }

    public Page<ChatMessageResponse> getMessagesByChannel(String channelId, String lastId, int page, int size) {
        // page와 size 값 검증 (음수일 경우 예외 처리)
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("페이지 번호와 크기는 양수여야 합니다.");
        }

        ObjectId objectId = null;

        // lastId를 ObjectId로 변환
        if (lastId != null && !lastId.isEmpty()) {
            objectId = new ObjectId(lastId);
        }

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size);

        // lastId가 있는 경우 이후 메시지 조회, 없는 경우 전체 조회
        Page<ChatMessage> messages;
        if (objectId != null) {
            messages = chatMessageRepository
                    .findByChannelIdAndIdGreaterThanOrderByTimestampDesc(channelId, objectId, pageable);
        } else {
            messages = chatMessageRepository
                    .findByChannelIdOrderByTimestampDesc(channelId, pageable);
        }

        // 결과를 ChatMessageResponse로 변환하여 반환
        return messages.map(msg -> new ChatMessageResponse(
                msg.getId(), // ObjectId를 문자열로 변환
                msg.getEmail(),
                msg.getContent(),
                msg.getWriter(),
                msg.getTimestamp()
        ));
    }





    // 메시지 삭제
    public void deleteMessage(String chatId) {
        chatMessageRepository.deleteById(chatId);
    }

    // 메시지 컨텐츠 업데이트
    public void updateMessage(String chatId, String reqMessage) {
        ChatMessage chatMessage = chatMessageRepository.findById(chatId).orElseThrow();

        if (!chatMessage.getContent().equals(reqMessage)) {
            chatMessageRepository.updateContent(chatId, reqMessage); // 변경된 내용만 저장
        }
    }

    // 메시지 조
    public Optional<ChatMessage> getChatMessage(String chatId) {
        try {
            ObjectId objectId = new ObjectId(chatId);
            return chatMessageRepository.findById(objectId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid chatId format", e);
            return Optional.empty();
        }
    }

    // 해당 채널의 모든 메시지 삭제
    @Transactional
    public void deleteChatMessageByChannelId(String channelId){
        chatMessageRepository.deleteChatMessageByChannelId(channelId);
    }
}
