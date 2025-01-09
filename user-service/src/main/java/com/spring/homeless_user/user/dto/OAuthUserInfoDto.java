package com.spring.homeless_user.user.dto;

import com.spring.homeless_user.user.entity.Provider;

public class OAuthUserInfoDto {

    private String id;
    private String email;        // OAuth 사용자 이메일
    private String name;         // 사용자 이름
    private Provider provider;     // OAuth 제공자(Google, GitHub 등)
    private String providerId;   // OAuth 제공자의 고유 사용자 ID

    // 생성자
    public OAuthUserInfoDto(String id,String email, String name, Provider provider, String providerId) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
    }

    public OAuthUserInfoDto(String id, String email, String name, String id1) {

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

    public Provider getProvider() {
        return this.provider;
    }

    // Setter 메서드
    public void setProvider(Provider provider) {
        this.provider = provider;
    }

}

