package com.homeless.chatservice.controller;

import com.homeless.chatservice.common.auth.JwtUtils;
import com.homeless.chatservice.common.exception.ChatMessageNotFoundException;
import com.homeless.chatservice.dto.ChatMessageResponse;
import com.homeless.chatservice.dto.CommonResDto;
import com.homeless.chatservice.entity.ChatMessage;
import com.homeless.chatservice.service.ChatHttpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ChatHttpController {

    private final ChatHttpService chatHttpService;
    private final JwtUtils jwtUtils;

    // 특정 채널의 메시지 조회
    @GetMapping("/ch/{channelId}")
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


    //메시지 삭제 : 토큰에서 이메일 가져오기 위한 헤더
    @DeleteMapping("/message/{chatId}")
    public ResponseEntity<?> deleteMessage(@PathVariable String chatId,
                                           @RequestHeader("Authorization") String authorizationHeader) {

        log.info("DeleteMapping chatId: {}, authorizationHeader: {}", chatId, authorizationHeader);
        try {
            // 1. 토큰 검사. (Bearer 떼고 검사)
            String tokenWithoutBearer = jwtUtils.validateToken(authorizationHeader);
            log.info("after validate token...");
            // 2. 토큰에서 이메일 가져오기
            String userEmail = jwtUtils.getEmailFromToken(tokenWithoutBearer);
            log.info("userEmail: {}", userEmail);
            // 3. chatId로 chatMessageOpt 가져오기
            Optional<ChatMessage> chatMessageOpt = chatHttpService.getChatMessage(chatId);
            // 4. Optional 객체의 값이 존재하는지 확인
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


    // 메시지 수정
    @PatchMapping("/message/{chatId}")
    public ResponseEntity<?> updateMessage(@PathVariable String chatId,
                                           @RequestBody String reqMessage) {
        try {
            chatHttpService.updateMessage(chatId, reqMessage);
            CommonResDto<Void> resDto = new CommonResDto<>(HttpStatus.OK, "메세지 수정 성공", null);
            return new ResponseEntity<>(resDto, HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // feign : 채널 삭제
    @DeleteMapping("/ch/{channelId}")
    public ResponseEntity<Void> deleteMessagesByChannel(@PathVariable String channelId) {
        chatHttpService.deleteChatMessageByChannelId(channelId);
        return ResponseEntity.noContent().build();

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
