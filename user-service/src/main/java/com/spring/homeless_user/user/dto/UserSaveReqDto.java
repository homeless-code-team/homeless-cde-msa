package com.spring.homeless_user.user.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class UserSaveReqDto {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Nickname is required")
    private String nickname;

    private String content;
    private String profileImage;

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
