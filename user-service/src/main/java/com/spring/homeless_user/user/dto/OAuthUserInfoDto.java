package com.spring.homeless_user.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter@Setter@ToString
@NoArgsConstructor
public class OAuthUserInfoDto {

    private String id;
    private String email;        // OAuth 사용자 이메일
    private String name;         // 사용자 이름


    // 생성자
    public OAuthUserInfoDto(String id,String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;

    }

    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


}

