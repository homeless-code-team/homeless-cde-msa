package com.homeless.chatservice.entity;

import com.homeless.chatservice.dto.MessageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

    private String channelId;
    private String writer;

    @NotNull(message = "Message content cannot be null")
    @Size(min = 1, max = 1000, message = "Message content must be between 1 and 1000 characters")
    private String content;

    private MessageType type;
}