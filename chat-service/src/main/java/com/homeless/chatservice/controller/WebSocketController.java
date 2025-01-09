package com.homeless.chatservice.controller;


import com.homeless.chatservice.dto.JoinMessage;
import com.homeless.chatservice.dto.LeaveMessage;
import com.homeless.chatservice.entity.MessageDto;
import com.homeless.chatservice.service.StompMessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    private final StompMessageService messageService;

    // 채팅 메시지 수신 및 저장
    @MessageMapping("chat.message.{channelId}") // 웹소켓을 통해 들어오는 메시지의 목적지를 정함.
    @Operation(summary = "메시지 전송", description = "메시지를 전송합니다.")
    public void sendMessage(@DestinationVariable String channelId, MessageDto chatMessage) { // @DestinationVariable로 url의 동적 부분을 파라미터로 받는다.
        log.info("Received message for channel: {}, chatMessage: {}", channelId, chatMessage);
        chatMessage.setChannelId(channelId);
        // 메시지 저장
        messageService.sendMessage(chatMessage);
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
