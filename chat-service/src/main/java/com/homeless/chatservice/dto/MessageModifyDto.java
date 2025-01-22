package com.homeless.chatservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageModifyDto {
    private String chatId;
    private String reqMessage;

}
