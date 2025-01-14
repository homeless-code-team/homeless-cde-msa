package com.playdata.homelesscode.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "chat-service")
public interface ChatServiceClient {

    // 채널 메시지 삭제
    @DeleteMapping("/api/v1/chats/ch/{channelId}")
    void deleteChatMessageByChannelId(@PathVariable("channelId") String channelId);
}
