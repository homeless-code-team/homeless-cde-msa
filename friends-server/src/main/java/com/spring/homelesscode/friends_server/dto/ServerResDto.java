package com.spring.homelesscode.friends_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServerResDto {
    private String email;
    private String serverId;
}
