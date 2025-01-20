package com.playdata.homelesscode.dto.server;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
public class ResignUserDto {

    private String serverId;
    private String email;


}
