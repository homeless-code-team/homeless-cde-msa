package com.spring.homeless_user.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
public class UserResponseDto {

    private String email;
    private String nickname;
    private String profileImage;


    public UserResponseDto(String email, String nickName, String profileImg) {
        this.email = email;
        this.nickname = nickName;
        this.profileImage = profileImg;
    }
}
