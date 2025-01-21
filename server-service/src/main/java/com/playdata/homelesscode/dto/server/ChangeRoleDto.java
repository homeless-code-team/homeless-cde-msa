package com.playdata.homelesscode.dto.server;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
public class ChangeRoleDto {

    private String id;
    private String email;
    private Role role;
}
