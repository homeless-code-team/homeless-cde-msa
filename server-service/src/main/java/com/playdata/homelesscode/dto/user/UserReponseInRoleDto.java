package com.playdata.homelesscode.dto.user;

import com.playdata.homelesscode.dto.server.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
public class UserReponseInRoleDto {

    private String id;
    private String nickname;
    private String email;
    private String profileImage;
    private Role role;

    public UserReponseInRoleDto(String id, String nickname, String email, String profileImage, Role role) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.profileImage = profileImage;
        this.role = role;
    }
}
