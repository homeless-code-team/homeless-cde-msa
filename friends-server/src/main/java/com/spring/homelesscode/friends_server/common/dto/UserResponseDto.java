package com.spring.homelesscode.friends_server.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
public class UserResponseDto {

    private String id;
    private String email;
    private String nickname;
    private String profileImage;

}