package com.homeless.chatservice.controller;


import com.homeless.chatservice.dto.ChannelResponse;
import com.homeless.chatservice.dto.ChannelType;
import com.homeless.chatservice.dto.CreateChannelRequest;
import com.homeless.chatservice.service.StompMessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelManagerController {

    private final StompMessageService messageService;

    @PostMapping
    @Operation(summary = "채팅 채널 생성")
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody CreateChannelRequest request) {
        String channelId = messageService.createChannel(request);
        ChannelResponse channelResponse
                = new ChannelResponse(
                        channelId,
                        request.getChannelName(),
                        request.getCreatorId(),
                        ChannelType.PUBLIC);
        return ResponseEntity.ok(channelResponse);
    }

    @DeleteMapping("/{channelId}")
    @Operation(summary = "채팅 채널 삭제")
    public ResponseEntity<Void> removeChannel(@PathVariable String channelId) {
        messageService.removeChannel(channelId);
        return ResponseEntity.ok().build();
    }


}
