package com.spring.homeless_user.user.dto;

import lombok.*;

@Getter@Setter@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ModifyDto {
    private String nickname;
    private String password;
    private String content;
}
