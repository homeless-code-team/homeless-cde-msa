package com.playdata.homelesscode.dto.channel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
public class ChannelUpdateDto {

    private String name;
    private String channelId;

}
