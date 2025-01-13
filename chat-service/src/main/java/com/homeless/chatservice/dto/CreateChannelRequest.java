package com.homeless.chatservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateChannelRequest {

    private String channelName;
    private String creatorId;
    private ChannelType channelType;    // PRIVATE, PUBLIC 등


}
