package com.homeless.chatservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter @Setter @ToString
public class JoinMessage {

    private String userId;
    private String userName;
    private MessageType messageType = MessageType.JOIN;

}
