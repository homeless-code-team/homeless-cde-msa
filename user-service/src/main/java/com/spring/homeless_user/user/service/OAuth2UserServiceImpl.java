package com.spring.homeless_user.user.service;

import com.spring.homeless_user.common.oauth.GithubOAuth2UserInfo;
import com.spring.homeless_user.common.oauth.GoogleOAuth2UserInfo;
import com.spring.homeless_user.common.oauth.OAuth2UserInfo;
import com.spring.homeless_user.user.entity.Provider;
import com.spring.homeless_user.user.entity.Role;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public OAuth2UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);
        // 제공자(provider) 구분
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Provider provider = Provider.fromRegistrationId(registrationId);

        log.info("registrationId:{}",registrationId);
        OAuth2UserInfo oauth2UserInfo = getOAuth2UserInfo(registrationId, oauth2User.getAttributes());
        log.info("oauth2UserInfo:{}",oauth2UserInfo);
        // registrationId는 google or github
        User user = userRepository.findByEmailAndProvider(oauth2UserInfo.getEmail(), registrationId)
                .map(existingUser -> {
                    existingUser.setNickname(oauth2UserInfo.getName());
                    return existingUser;
                })
                .orElse(User.builder()
                        .email(oauth2UserInfo.getEmail())
                        .nickname(oauth2UserInfo.getName())
                        .provider(provider)
                        .build());

        userRepository.save(user);

        log.info("user:{}",user);
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(Role.USER.toString())),
                oauth2User.getAttributes(),
                oauth2UserInfo.getNameAttributeKey()
        );
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equals("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equals("github")) {
            return new GithubOAuth2UserInfo(attributes);
        }
        throw new RuntimeException("Sorry! Login with " + registrationId + " is not supported yet.");
    }
}
