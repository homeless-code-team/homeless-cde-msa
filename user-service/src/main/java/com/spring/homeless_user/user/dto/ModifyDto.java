package com.spring.homeless_user.user.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter@Setter@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ModifyDto {
    private String nickname;
    private String password;
    private String content;
    private MultipartFile profileImage;


}
