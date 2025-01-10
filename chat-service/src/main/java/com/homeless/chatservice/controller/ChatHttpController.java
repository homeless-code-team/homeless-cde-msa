package com.homeless.chatservice.controller;

import com.homeless.chatservice.auth.JwtTokenProvider;
import com.homeless.chatservice.dto.ChatMessageCreateCommand;
import com.homeless.chatservice.dto.ChatMessageRequest;
import com.homeless.chatservice.dto.ChatMessageResponse;
import com.homeless.chatservice.dto.CommonResDto;
import com.homeless.chatservice.entity.ChatMessage;
import com.homeless.chatservice.exception.ChatMessageNotFoundException;
import com.homeless.chatservice.repository.ChatMessageRepository;
import com.homeless.chatservice.service.ChatHttpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/chats")
public class ChatHttpController {

    private final ChatHttpService chatHttpService;
    private final JwtTokenProvider jwtTokenProvider;

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
            if (chatMessage.content() == null || chatMessage.content().isEmpty()) {
                throw new IllegalArgumentException("Message text cannot be empty");
            }

            // 채팅 메시지 생성
            ChatMessageCreateCommand chatMessageCreateCommand =
                    new ChatMessageCreateCommand("test-server", channelId, chatMessage.email(), chatMessage.content(), chatMessage.writer());

            // 메시지 저장 후 생성된 chatId
            String chatId = chatHttpService.createChatMessage(chatMessageCreateCommand);

            // 저장된 메시지 응답
            //ChatMessageResponse response = new ChatMessageResponse(chatId, chatMessage.text(), chatMessage.writer(), chatMessage.timestamp());
            Map<String, Object> result = new HashMap<>();
            CommonResDto<Object> commonResDto = new CommonResDto<>(HttpStatus.OK, "메시지 저장 완료", result);

            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<?> deleteMessage(@PathVariable String chatId,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        try {

            // 1. 헤더 검사
            if (!authorizationHeader.startsWith("Bearer ")) {
                return new ResponseEntity<>(new CommonResDto<>(HttpStatus.BAD_REQUEST, "Authorization 헤더 형식이 잘못되었습니다.", null),
                        HttpStatus.BAD_REQUEST);
            }
            // 2. 헤더에서 토큰가져오기
            String token = authorizationHeader.substring(7);

            // 3. 토큰 유효성 검사
            if (!jwtTokenProvider.validateToken(token)) {
                return new ResponseEntity<>(new CommonResDto<>(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰", null),
                        HttpStatus.UNAUTHORIZED);
            }
            // 4. 토큰에서 이메일 가져오기
            String userEmail = jwtTokenProvider.getEmailFromToken(token);

            // 5. chatId로 chatMessageOpt 가져오기
            Optional<ChatMessage> chatMessageOpt = chatHttpService.getChatMessage(chatId);

            // Optional 객체의 값이 존재하는지 확인
            if (chatMessageOpt.isPresent()) {
                ChatMessage chatMessage = chatMessageOpt.get();

                // 작성자 확인 또는 관리자 권한 확인
                if (chatMessage.getEmail().equals(userEmail)) {
                    chatHttpService.deleteMessage(chatId);
                    CommonResDto<Void> resDto = new CommonResDto<>(HttpStatus.OK, "메세지 삭제 성공: " + chatId, null);
                    return new ResponseEntity<>(resDto, HttpStatus.OK);
                } else {
                    CommonResDto<Void> resDto = new CommonResDto<>(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.", null);
                    return new ResponseEntity<>(resDto, HttpStatus.FORBIDDEN);
                }
            } else {
                throw new ChatMessageNotFoundException("Chat message not found with id: " + chatId);
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }


    @PatchMapping("/{chatId}")
    public ResponseEntity<?> updateMessage(@PathVariable String chatId,
                                           @RequestBody ChatMessageRequest reqMessage) {
        try {
            chatHttpService.updateMessage(chatId, reqMessage);
            CommonResDto<Void> resDto = new CommonResDto<>(HttpStatus.OK, "메세지 수정 성공", null);
            return new ResponseEntity<>(resDto, HttpStatus.OK);
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
