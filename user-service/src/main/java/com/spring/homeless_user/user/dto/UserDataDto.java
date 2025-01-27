package com.spring.homeless_user.user.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDataDto {

    private String nickname;
    private String email;
    private String profileImage;
    private String contents;
}
