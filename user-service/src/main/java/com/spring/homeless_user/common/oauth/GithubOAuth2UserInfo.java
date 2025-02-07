package com.spring.homeless_user.common.oauth;

import lombok.Getter;

import java.util.Map;

@Getter
public class GithubOAuth2UserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes;

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        return ((Integer)attributes.get("id")).toString();
    }

    @Override
    public String getName() {
        return (String )attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String )attributes.get("email");
    }

    @Override
    public String getNameAttributeKey() {
       return "id";
    }
}
