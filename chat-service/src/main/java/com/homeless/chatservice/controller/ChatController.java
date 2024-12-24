package com.homeless.chatservice.controller;

import com.homeless.chatservice.dto.ChatMessageCreateCommand;
import com.homeless.chatservice.dto.ChatMessageRequest;
import com.homeless.chatservice.dto.ChatMessageResponse;
import com.homeless.chatservice.service.ChatMessageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageService chatMessageService;

    // WebSocket 메시지 전송 처리
    @MessageMapping("/{serverId}/{channelId}/send")
    @SendTo("/topic/{serverId}/{channelId}")
    public ChatMessageResponse sendMessageWebSocket(
        @DestinationVariable Long serverId,
        @DestinationVariable Long channelId,
        @Payload ChatMessageRequest chatMessage) {
        try {
            // 채팅 메시지 생성
            ChatMessageCreateCommand chatMessageCreateCommand =
                new ChatMessageCreateCommand(serverId, channelId, chatMessage.text(), chatMessage.writer());

            // 메시지 저장
            String chatId = chatMessageService.createChatMessage(chatMessageCreateCommand);

            // 저장된 메시지 응답
            return new ChatMessageResponse(chatId, chatMessage.text(), chatMessage.writer(), chatMessage.timestamp());
        } catch (Exception e) {
            throw new RuntimeException("Error handling chat message", e);
        }
    }

    // HTTP POST 요청을 통한 메시지 전송 처리
    @PostMapping("/{serverId}/{channelId}/send")
    public ResponseEntity<ChatMessageResponse> sendMessageHttp(
        @PathVariable Long serverId,
        @PathVariable Long channelId,
        @RequestBody ChatMessageRequest chatMessage) {
        try {
            // 채팅 메시지 생성
            ChatMessageCreateCommand chatMessageCreateCommand =
                new ChatMessageCreateCommand(serverId, channelId, chatMessage.text(), chatMessage.writer());

            // 메시지 저장
            String chatId = chatMessageService.createChatMessage(chatMessageCreateCommand);

            // 저장된 메시지 응답
            return ResponseEntity.ok(new ChatMessageResponse(chatId, chatMessage.text(), chatMessage.writer(), chatMessage.timestamp()));
        } catch (Exception e) {
            throw new RuntimeException("Error handling chat message", e);
        }
    }

    // 특정 채널의 메시지 조회
    @GetMapping("/{serverId}/{channelId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
        @PathVariable Long serverId,
        @PathVariable Long channelId) {
        try {
            // 메시지 조회
            List<ChatMessageResponse> messages = chatMessageService.getMessagesByChannel(channelId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving chat messages", e);
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
