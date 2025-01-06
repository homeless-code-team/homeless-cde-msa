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
    private String serveImg;
    private String userId;


    public ServerResponseDto(String id, String tag, String title, String serveImg, String userId) {

        this.id = id;
        this.title = title;
        this.tag = tag;
        this.serveImg = serveImg;
        this.userId = userId;
    }
}
