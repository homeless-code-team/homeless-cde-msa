package com.homeless.chatservice.entity;

import com.homeless.chatservice.dto.MessageType;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

    private String channelId;
    private String content;
    private MessageType type;

}