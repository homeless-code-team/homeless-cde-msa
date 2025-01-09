package com.homeless.chatservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class LeaveMessage {

    private String userId;
    private MessageType type = MessageType.LEAVE;

}
