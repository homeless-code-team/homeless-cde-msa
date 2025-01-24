package com.playdata.homelesscode.dto.server;

import com.playdata.homelesscode.entity.Server;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class inviteServerList {

    private String serverId;
    private String title;
    private String serverImg;

    public inviteServerList(Server server) {
        this.serverId = server.getId();
        this.title = server.getTitle();
        this.serverImg = server.getServerImg();
    }
}
