package com.homeless.chatservice.dto;

import lombok.Builder;

@Builder
public record ChatFileRequest(String serverId,
                              String email,
                              String writer,
                              String content,
                              MessageType messageType,
                              String fileUrl,
                              String fileName) {

}
