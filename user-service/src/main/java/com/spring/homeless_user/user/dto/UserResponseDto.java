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

    private String id;
    private String email;
    private String nickname;
    private String profileImage;


    public UserResponseDto(String id,String email, String nickName, String profileImg) {
        this.id = id;
        this.email = email;
        this.nickname = nickName;
        this.profileImage = profileImg;
    }
}
