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
    private String serveImg;


    public ServerResponseDto(String id, String tag, String serveImg) {

        this.id = id;
        this.tag = tag;
        this.serveImg = serveImg;
    }
}
