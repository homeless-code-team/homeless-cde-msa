package com.spring.homeless_user.user.dto;

import com.spring.homeless_user.user.entity.AddStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerDto {
    private Long serverId;
    private AddStatus addStatus;
}
