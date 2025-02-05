package com.spring.homeless_user.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter@Setter@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ModifyDto {
    private String nickname;

    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~])(?=\\S+$).{8,16}$",
            message = "비밀번호는 8~16자이며, 소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;
    private String content;
    private MultipartFile profileImage;


}
