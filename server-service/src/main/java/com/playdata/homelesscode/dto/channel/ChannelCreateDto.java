package com.playdata.homelesscode.dto.channel;

import com.playdata.homelesscode.entity.Channel;
import com.playdata.homelesscode.entity.Server;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
public class ChannelCreateDto {

    private String name;
    private String serverId;


    public Channel toEntity(Server server) {
        return Channel.builder()
                .name(name)
                .server(server)
                .build();
    }
}
