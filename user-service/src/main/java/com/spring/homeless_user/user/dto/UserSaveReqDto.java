package com.spring.homeless_user.user.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Data
@NoArgsConstructor
public class UserSaveReqDto {

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "Email is required")
    private String email;

    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~])(?=\\S+$).{8,16}$",
            message = "비밀번호는 8~16자이며, 소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Nickname is required")
    private String nickname;

    private String content;
    private MultipartFile profileImage;

    @JsonCreator
    public UserSaveReqDto(
            @JsonProperty("email") String email,
            @JsonProperty("password") String password,
            @JsonProperty("nickname") String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

}
