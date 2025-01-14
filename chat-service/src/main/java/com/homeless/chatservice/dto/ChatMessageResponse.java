package com.homeless.chatservice.dto;

import java.time.LocalDateTime;
import lombok.Builder;



@Builder
public record ChatMessageResponse(String id, String email, String content, String writer, Long timestamp) {
}