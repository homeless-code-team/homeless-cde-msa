package com.spring.homeless_user.common.oauth;

public interface OAuth2UserInfo {

    String getId();
    String getName();
    String getEmail();
    String getNameAttributeKey();

}
