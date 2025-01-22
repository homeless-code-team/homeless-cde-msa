package com.homeless.chatservice.controller;


import com.homeless.chatservice.common.auth.JwtUtils;
import com.homeless.chatservice.common.exception.TokenValidationException;
import com.homeless.chatservice.dto.*;
import com.homeless.chatservice.entity.ChatMessage;
import com.homeless.chatservice.entity.MessageDto;
import com.homeless.chatservice.service.ChatHttpService;
import com.homeless.chatservice.service.FileService;
import com.homeless.chatservice.service.StompMessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    private final StompMessageService messageService;
    private final ChatHttpService chatHttpService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final JwtUtils jwtUtils;
    private final FileService fileService;

    // 채팅 메시지 수신 및 저장
    @MessageMapping("chat.message.{channelId}") // 웹소켓을 통해 들어오는 메시지의 목적지를 정함.
    @Operation(summary = "메시지 전송", description = "메시지를 전송합니다.")
    @Transactional
    public void sendMessage(@DestinationVariable String channelId,
                            ChatMessageRequest chatReqDto) { // @DestinationVariable로 url의 동적 부분을 파라미터로 받는다.


        log.info("Received message for Server: {}, channel: {}, chatMessage: {}", chatReqDto.serverId(), channelId, chatReqDto);
        log.info("fileUrl: {}", chatReqDto.fileUrl());
        try {

            // 1. 컨텐츠 유효성 검사
            if (chatReqDto.content() == null && chatReqDto.fileUrl() == null) {
                throw new IllegalArgumentException("Message text cannot be empty");
            }

            // 2. 요청 메시지로 채팅 생성 객체 제작
            ChatMessageCreateCommand chatMessageCreateCommand =
                    new ChatMessageCreateCommand(chatReqDto.serverId(), channelId, chatReqDto.email(),
                            chatReqDto.writer(), chatReqDto.content(), chatReqDto.messageType(), chatReqDto.fileUrl(), chatReqDto.fileName());

            // 3. 메시지 저장 후 생성된 chatId
            String chatId = chatHttpService.createChatMessage(chatMessageCreateCommand);

            // 4. ChatMessageRequest -> MessageDto 변환
            MessageDto messageDto = MessageDto.builder()
                    .chatId(chatId) // DB 저장 후 채워질 값
                    .channelId(channelId)
                    .channelType(ChannelType.PUBLIC)
                    .email(chatReqDto.email())
                    .writer(chatReqDto.writer())
                    .content(chatReqDto.content())
                    .messageType(chatReqDto.messageType())
                    .fileUrl(chatReqDto.fileUrl())
                    .fileName(chatReqDto.fileName())
                    .build();

            // 5. 메시지 DB 저장
            messageService.sendMessage(messageDto);
            // 6. 성공 응답
            Map<String, Object> result = new HashMap<>();
            result.put("chatId", chatId);
            CommonResDto<Object> commonResDto = new CommonResDto<>(HttpStatus.OK, "메시지 저장 완료", result);

            // 8.클라이언트로 응답 전송 (웹소켓을 통해)
            simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, commonResDto);

        } catch (IllegalArgumentException e) {
            log.error("Invalid message content for channel: {}. Error: {}", channelId, e.getMessage());
            // 예외 처리: 파라미터 오류
            Map<String, Object> errorResult = new HashMap<>();
            CommonResDto<Object> errorResDto = new CommonResDto<>(HttpStatus.BAD_REQUEST, "Invalid message content", errorResult);
            simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResDto);
        } catch (Exception e) {
            log.error("Error while sending message for channel: {}. Error: {}", channelId, e.getMessage(), e);
            // 예외 처리: 저장 실패
            Map<String, Object> errorResult = new HashMap<>();
            CommonResDto<Object> errorResDto = new CommonResDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save message", errorResult);
            simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResDto);
        }
    }


    @MessageMapping("chat.message.delete.{channelId}")
    @SendTo("/exchange/chat.exchange/chat.channel.{channelId}")
    @Transactional
    public void deleteMessage(@DestinationVariable String channelId,
                              String chatId,
                              @Header("Authorization") String authorizationHeader) {

        log.info("WebSocket delete request - chatId: {}, channelId: {}, authorizationHeader: {}", chatId, channelId, authorizationHeader);

        try {
            // 1. 토큰 검사 (Bearer 떼고 검사)
            String tokenWithoutBearer = jwtUtils.validateToken(authorizationHeader);
            log.info("Token validated");

            // 2. 토큰에서 이메일 가져오기
            String userEmail = jwtUtils.getEmailFromToken(tokenWithoutBearer);
            log.info("User email: {}", userEmail);


            // 3. chatId로 메시지 가져오기
            Optional<ChatMessage> chatMessageOpt = chatHttpService.getChatMessage(chatId);
            log.info("Chat message: {}", chatId);
            log.info("chatMessageOpt: {}", chatMessageOpt);
            // 4. 메시지가 존재하는지 확인
            if (chatMessageOpt.isPresent()) {
                ChatMessage chatMessage = chatMessageOpt.get();

                // 5. 작성자 또는 관리자 권한 확인
                if (chatMessage.getEmail().equals(userEmail)) {
                    // 메시지 삭제
                    chatHttpService.deleteMessage(chatId);
                    log.info("Message with chatId {} deleted successfully : 메시지 삭제 성공!!!", chatId);

                    // 삭제된 메시지를 클라이언트에게 전송
                    Map<String, Object> result = new HashMap<>();
                    result.put("status", "success");
                    result.put("message", "Message deleted: 메시지 삭제됨.");
                    result.put("deletedChatId", chatId);
                    simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, result);
                } else {
                    log.warn("User does not have permission to delete message: {}", chatId);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "Permission denied");
                    simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResult);
                }
            } else {
                log.error("Chat message not found: {} : 메시지 객체 찾지못함.", chatId);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "Chat message not found");
                simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResult);
            }
        } catch (TokenValidationException e) {
            log.error("Token validation failed: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "error");
            errorResult.put("message", "Token validation failed");
            simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResult);
        } catch (Exception e) {
            log.error("Error while deleting message: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "error");
            errorResult.put("message", "Failed to delete message");
            simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResult);
        }
    }

    @MessageMapping("chat.message.update.{channelId}")
    @SendTo("/exchange/chat.exchange/chat.channel.{channelId}")
    @Transactional
    public void updateMessage(@DestinationVariable String channelId,
                              MessageModifyDto dto,
                              @Header("Authorization") String authorizationHeader) {

        log.info("WebSocket update request - chatId: {}, channelId: {}, authorizationHeader: {},  reqMessage: {}",
                dto.getChatId(), channelId, authorizationHeader, dto.getReqMessage());

        try {
            // 1. 토큰 검사 (Bearer 떼고 검사)
            String tokenWithoutBearer = jwtUtils.validateToken(authorizationHeader);
            log.info("Token validated");

            // 2. 토큰에서 이메일 가져오기
            String userEmail = jwtUtils.getEmailFromToken(tokenWithoutBearer);
            log.info("User email: {}", userEmail);

            // 3. chatId로 메시지 가져오기
            Optional<ChatMessage> chatMessageOpt = chatHttpService.getChatMessage(dto.getChatId());
            log.info("Chat message: {}", dto.getReqMessage());
            log.info("chatMessageOpt: {}", chatMessageOpt);

            // 4. 메시지가 존재하는지 확인
            if (chatMessageOpt.isPresent()) {
                ChatMessage chatMessage = chatMessageOpt.get();

                // 5. 작성자 또는 관리자 권한 확인
                if (chatMessage.getEmail().equals(userEmail)) {
                    // 메시지 내용 수정
                    chatMessage.setContent(dto.getReqMessage());
                    chatHttpService.updateMessage(dto.getChatId(), dto.getReqMessage()); // 메시지 수정 후 저장
                    log.info("Message with chatId {} updated successfully: 내용 수정 완료", dto.getChatId());

                    // 수정된 메시지를 클라이언트에게 전송
                    Map<String, Object> result = new HashMap<>();
                    result.put("chatId", dto.getChatId());
                    result.put("reqMessage", dto.getReqMessage());

                    // CommonResDto 생성
                    CommonResDto<Object> commonResDto = new CommonResDto<>(HttpStatus.OK, "Message updated successfully", result);

                    // 클라이언트로 응답 전송 (웹소켓을 통해)
                    simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, commonResDto);
                } else {
                    log.warn("User does not have permission to update message: {}", dto.getChatId());
                    // 권한 없을 때 에러 응답
                    Map<String, Object> errorResult = new HashMap<>();
                    CommonResDto<Object> errorResDto = new CommonResDto<>(HttpStatus.FORBIDDEN, "Permission denied", errorResult);
                    simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResDto);
                }
            } else {
                log.error("Chat message not found: {}", dto.getChatId());
                // 메시지 없을 때 에러 응답
                Map<String, Object> errorResult = new HashMap<>();
                CommonResDto<Object> errorResDto = new CommonResDto<>(HttpStatus.NOT_FOUND, "Chat message not found", errorResult);
                simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResDto);
            }
        } catch (TokenValidationException e) {
            log.error("Token validation failed: {}", e.getMessage(), e);
            // 토큰 검증 실패 에러 응답
            Map<String, Object> errorResult = new HashMap<>();
            CommonResDto<Object> errorResDto = new CommonResDto<>(HttpStatus.UNAUTHORIZED, "Token validation failed", errorResult);
            simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResDto);
        } catch (Exception e) {
            log.error("Error while updating message: {}", e.getMessage(), e);
            // 일반적인 오류 에러 응답
            Map<String, Object> errorResult = new HashMap<>();
            CommonResDto<Object> errorResDto = new CommonResDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update message", errorResult);
            simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResDto);
        }
    }

}
