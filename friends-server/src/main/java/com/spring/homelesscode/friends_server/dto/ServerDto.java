package com.spring.homelesscode.friends_server.dto;

import com.spring.homelesscode.friends_server.entity.AddStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerDto {
    private String serverId;
    private AddStatus addStatus;
}
