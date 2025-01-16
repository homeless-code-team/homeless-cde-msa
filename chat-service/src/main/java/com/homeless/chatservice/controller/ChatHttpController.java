package com.homeless.chatservice.controller;

import com.homeless.chatservice.common.auth.JwtUtils;
import com.homeless.chatservice.common.exception.ChatMessageNotFoundException;
import com.homeless.chatservice.dto.ChatMessageResponse;
import com.homeless.chatservice.dto.CommonResDto;
import com.homeless.chatservice.entity.ChatMessage;
import com.homeless.chatservice.service.ChatHttpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
            @PathVariable String channelId,
            @RequestParam(required = false) String lastId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        try {
            // size 값 검증
            if (size <= 0) {
                throw new IllegalArgumentException("size는 1 이상의 값이어야 합니다.");
            }

            // 메시지 조회 (Page 형태로 반환)
            Page<ChatMessageResponse> messages = chatHttpService.getMessagesByChannel(channelId, lastId,page, size);

            Map<String, Object> result = new HashMap<>();
            result.put("messages", messages.getContent()); // 메시지 목록
            result.put("totalElements", messages.getTotalElements()); // 총 메시지 수
            result.put("totalPages", messages.getTotalPages()); // 총 페이지 수
            result.put("currentPage", messages.getNumber()); // 현재 페이지

            CommonResDto<Object> commonResDto = new CommonResDto<>(HttpStatus.OK, "메시지 조회 완료", result);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // size 값 검증 실패시 처리
            return new ResponseEntity<>(new CommonResDto<>(HttpStatus.BAD_REQUEST, e.getMessage(), null), HttpStatus.BAD_REQUEST);
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
