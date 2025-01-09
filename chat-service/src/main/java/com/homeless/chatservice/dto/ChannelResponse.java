package com.homeless.chatservice.dto;

import lombok.*;

@Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelResponse {

    private String channelId;
    private String channelName;
    private String creatorId;
    private ChannelType channelType;

}
