package com.homeless.chatservice.controller;

import com.homeless.chatservice.dto.ChatMessageResponse;
import com.homeless.chatservice.dto.CommonResDto;
import com.homeless.chatservice.service.ChatHttpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/chats")
@Slf4j
public class ChatHttpController {

    private final ChatHttpService chatHttpService;

    // 특정 채널의 메시지 조회
    @GetMapping("/ch/{channelId}")
    public ResponseEntity<?> getMessages(
            @PathVariable String channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // size 값 검증
            if (size <= 0) {
                throw new IllegalArgumentException("size는 1 이상의 값이어야 합니다.");
            }

            // 메시지 조회 (Page 형태로 반환)
            Page<ChatMessageResponse> messages = chatHttpService.getMessagesByChannel(channelId, page, size);

            Map<String, Object> result = new HashMap<>();
            result.put("messages", messages.getContent()); // 메시지 목록
            result.put("totalElements", messages.getTotalElements()); // 총 메시지 수
            result.put("totalPages", messages.getTotalPages()); // 총 페이지 수
            result.put("currentPage", messages.getNumber()); // 현재 페이지
            result.put("isLast", messages.isLast()); // 마지막 페이지 여부

            CommonResDto<Object> commonResDto = new CommonResDto<>(HttpStatus.OK, "메시지 조회 완료", result);

            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new CommonResDto<>(HttpStatus.BAD_REQUEST, e.getMessage(), null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/search")
    public Page<ChatMessageResponse> searchMessages(
            @RequestParam String channelId,
            @RequestParam String keyword,
            @RequestParam String category,
            @RequestParam int page,
            @RequestParam int size) {
        if (category.equals("content"))
            return chatHttpService.searchMessagesByChannel(channelId, keyword, page, size);
        if (category.equals("nickname"))
            return chatHttpService.searchMessagesByWriter(channelId, keyword, page, size);
        else
            return Page.empty();
    }

    // feign : 채널 삭제
    @DeleteMapping("/ch/{channelId}")
    public ResponseEntity<Void> deleteMessagesByChannel(@PathVariable String channelId) throws Exception {
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
