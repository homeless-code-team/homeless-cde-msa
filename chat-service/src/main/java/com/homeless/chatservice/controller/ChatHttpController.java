package com.homeless.chatservice.controller;

import com.homeless.chatservice.dto.ChatMessageCreateCommand;
import com.homeless.chatservice.dto.ChatMessageRequest;
import com.homeless.chatservice.dto.ChatMessageResponse;
import com.homeless.chatservice.repository.ChatMessageRepository;
import com.homeless.chatservice.service.ChatHttpService;
import com.homeless.chatservice.service.StompMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/chats")
public class ChatHttpController {

    private final StompMessageService stompMessageService;
    private final ChatHttpService chatHttpService;
    private final RabbitTemplate rabbitTemplate;


    // 특정 채널의 메시지 조회
    @GetMapping("/{serverId}/{channelId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long serverId,
            @PathVariable Long channelId) {
        try {
            // 메시지 조회
            List<ChatMessageResponse> messages = chatHttpService.getMessagesByChannel(channelId);
            // api 에서 요청한 serverId 내에

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            throw new RuntimeException("Error: Not resolve getMessages() [GET]", e);
        }
    }

    // HTTP POST 요청을 통한 메시지 전송 처리
    @PostMapping("/{serverId}/{channelId}")
    public ResponseEntity<ChatMessageResponse> sendMessageHttp(
            @PathVariable Long serverId,
            @PathVariable Long channelId,
            @RequestBody ChatMessageRequest chatMessage) {
        try {
            // 채팅 메시지 생성

            ChatMessageCreateCommand chatMessageCreateCommand =
                    new ChatMessageCreateCommand(serverId, channelId, chatMessage.text(), chatMessage.writer());

            // 메시지 저장
            String chatId = chatHttpService.createChatMessage(chatMessageCreateCommand);

            rabbitTemplate.convertAndSend("chatQueue", chatMessage);

            // 저장된 메시지 응답
            return ResponseEntity.ok(new ChatMessageResponse(chatId, chatMessage.text(), chatMessage.writer(), chatMessage.timestamp()));
        } catch (Exception e) {
            throw new RuntimeException("Error: Not resolve sendMessageHttp() [POST]", e);
        }
    }
    // WebSocket 예외 처리
    @MessageExceptionHandler
    public void handleMessageException(RuntimeException e) {
        System.err.println(e.getMessage());
    }

    // HTTP 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
