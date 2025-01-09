package com.homeless.chatservice.controller;

import com.homeless.chatservice.dto.ChatMessageCreateCommand;
import com.homeless.chatservice.dto.ChatMessageRequest;
import com.homeless.chatservice.dto.ChatMessageResponse;
import com.homeless.chatservice.dto.CommonResDto;
import com.homeless.chatservice.service.ChatHttpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/chats")
public class ChatHttpController {

    private final ChatHttpService chatHttpService;

    // 특정 채널의 메시지 조회
    @GetMapping("/{channelId}")
    public ResponseEntity<?> getMessages(
            @PathVariable String channelId) {
        try {
            // 메시지 조회
            List<ChatMessageResponse> messages = chatHttpService.getMessagesByChannel(channelId);

            Map<String, Object> result = new HashMap<>();
            result.put("messages", messages);
            CommonResDto<Object> commonResDto = new CommonResDto<>(HttpStatus.OK, "메시지 조회 완료", result);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // HTTP POST 요청을 통한 메시지 전송 처리
    @PostMapping("/{channelId}")
    public ResponseEntity<?> sendMessageHttp(
            @PathVariable String channelId,
            @RequestBody ChatMessageRequest chatMessage) {
        try {
            // 유효성 검사 (예시)
            if (chatMessage.text() == null || chatMessage.text().isEmpty()) {
                throw new IllegalArgumentException("Message text cannot be empty");
            }

            // 채팅 메시지 생성
            ChatMessageCreateCommand chatMessageCreateCommand =
                    new ChatMessageCreateCommand("test-server", channelId, chatMessage.text(), chatMessage.writer());

            // 메시지 저장 후 생성된 chatId
            String chatId = chatHttpService.createChatMessage(chatMessageCreateCommand);

            // 저장된 메시지 응답
            ChatMessageResponse response = new ChatMessageResponse(chatId, chatMessage.text(), chatMessage.writer(), chatMessage.timestamp());
            Map<String, Object> result = new HashMap<>();
            result.put("chatMessage", response);
            CommonResDto<Object> commonResDto = new CommonResDto<>(HttpStatus.OK, "메시지 전송 완료", result);

            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // HTTP 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        // 예외에 따라 다르게 응답할 수 있음
        String errorMessage = e.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // 예외에 따른 적절한 처리 (예: 사용자 입력 오류일 경우 BAD_REQUEST)
        if (e instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(errorMessage, status);
    }
}
