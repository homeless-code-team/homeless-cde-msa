package com.spring.homelesscode.friends_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServerResDto {
    private String receiver;
    private String serverId;
}
