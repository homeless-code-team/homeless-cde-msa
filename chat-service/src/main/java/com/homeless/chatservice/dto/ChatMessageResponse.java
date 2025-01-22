package com.homeless.chatservice.dto;

import lombok.Builder;



@Builder
public record ChatMessageResponse(String id,
                                  String email,
                                  String content,
                                  String writer,
                                  Long timestamp,
                                  String fileUrl,
                                  String fileName) {
}
