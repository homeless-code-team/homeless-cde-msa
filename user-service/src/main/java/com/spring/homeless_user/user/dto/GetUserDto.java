package com.spring.homeless_user.user.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GetUserDto {
    private String nickname;
    private String Email;
    private String content;
    private String profileImage;
}
