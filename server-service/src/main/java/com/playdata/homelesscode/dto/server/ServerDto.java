package com.playdata.homelesscode.dto.server;

import com.playdata.homelesscode.entity.AddStatus;
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
