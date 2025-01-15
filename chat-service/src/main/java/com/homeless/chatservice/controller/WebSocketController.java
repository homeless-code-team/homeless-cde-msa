package com.homeless.chatservice.controller;


import com.homeless.chatservice.dto.*;
import com.homeless.chatservice.entity.MessageDto;
import com.homeless.chatservice.service.ChatHttpService;
import com.homeless.chatservice.service.StompMessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    private final StompMessageService messageService;
    private final ChatHttpService chatHttpService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    // 채팅 메시지 수신 및 저장
    @MessageMapping("chat.message.{channelId}") // 웹소켓을 통해 들어오는 메시지의 목적지를 정함.
    @Operation(summary = "메시지 전송", description = "메시지를 전송합니다.")
    public void sendMessage(@DestinationVariable String channelId,
                            ChatMessageRequest chatReqDto) { // @DestinationVariable로 url의 동적 부분을 파라미터로 받는다.


        log.info("Received message for Server: {}, channel: {}, chatMessage: {}", chatReqDto.serverId(), channelId, chatReqDto);
        try {

            // 1. 컨텐츠 유효성 검사
            if (chatReqDto.content() == null || chatReqDto.content().isEmpty()) {
                throw new IllegalArgumentException("Message text cannot be empty");
            }

            // 2. 요청 메시지로 채팅 생성 객체 제작
            ChatMessageCreateCommand chatMessageCreateCommand =
                    new ChatMessageCreateCommand(chatReqDto.serverId(), channelId, chatReqDto.email(),
                            chatReqDto.writer(),chatReqDto.content(),chatReqDto.messageType());

            // 3. 메시지 저장 후 생성된 chatId
            String chatId = chatHttpService.createChatMessage(chatMessageCreateCommand);

            // 2. ChatMessageRequest -> MessageDto 변환
            MessageDto messageDto = MessageDto.builder()
                    .chatId(chatId) // DB 저장 후 채워질 값
                    .channelId(channelId)
                    .channelType(ChannelType.PUBLIC)
                    .email(chatReqDto.email())
                    .writer(chatReqDto.writer())
                    .content(chatReqDto.content())
                    .messageType(chatReqDto.messageType())
                    .build();

            // 메시지 저장
            messageService.sendMessage(messageDto);
            // 성공 응답
            Map<String, Object> result = new HashMap<>();
            CommonResDto<Object> commonResDto = new CommonResDto<>(HttpStatus.OK, "메시지 저장 완료", result);

            // 클라이언트로 응답 전송 (웹소켓을 통해)
            simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, commonResDto);

        } catch (IllegalArgumentException e) {
            log.error("Invalid message content for channel: {}. Error: {}", channelId, e.getMessage());
            // 예외 처리: 클라이언트로 에러 응답 전송
            Map<String, Object> errorResult = new HashMap<>();
            CommonResDto<Object> errorResDto = new CommonResDto<>(HttpStatus.BAD_REQUEST, "Invalid message content", errorResult);
            simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResDto);
        } catch (Exception e) {
            log.error("Error while sending message for channel: {}. Error: {}", channelId, e.getMessage(), e);
            // 예외 처리: 클라이언트로 에러 응답 전송
            Map<String, Object> errorResult = new HashMap<>();
            CommonResDto<Object> errorResDto = new CommonResDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save message", errorResult);
            simpMessagingTemplate.convertAndSend("/exchange/chat.exchange/chat.channel." + channelId, errorResDto);
        }
    }

    @MessageMapping("chat.join.{channelId}")
    @Operation(summary = "채널 입장", description = "채팅 채널에 입장합니다.")
    public void joinChannel(
            @DestinationVariable String channelId,
            JoinMessage joinMessage
    ) {
        log.info("User {} joining channel {}", joinMessage.getUserId(), channelId);
        messageService.handleJoinChannel(channelId, joinMessage);
    }

    @MessageMapping("chat.leave.{channelId}")
    @Operation(summary = "채널 퇴장", description = "채팅 채널에서 퇴장합니다.")
    public void leaveChannel(
            @DestinationVariable String channelId,
            LeaveMessage leaveMessage
    ) {
        log.info("User {} leaving channel {}", leaveMessage.getUserId(), channelId);
        messageService.handleLeaveChannel(channelId, leaveMessage);
    }


}
