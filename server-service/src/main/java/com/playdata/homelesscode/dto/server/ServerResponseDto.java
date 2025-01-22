package com.playdata.homelesscode.dto.server;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
public class ServerResponseDto {

    private String id;
    private String tag;
    private String title;
    private String serverImg;
    private String email;
    private int serverType;
    private Role role;


    public ServerResponseDto(String id, String tag, String title, String serveImg, String email, int serverType, Role role) {

        this.id = id;
        this.title = title;
        this.tag = tag;
        this.serverImg = serveImg;
        this.email = email;
        this.serverType = serverType;
        this.role = role;

    }
}
