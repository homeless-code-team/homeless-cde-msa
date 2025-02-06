package com.spring.homeless_user.user.service;

import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
@Slf4j
@Service
public class OAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    public OAuth2UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info(userRequest.getClientRegistration().getRegistrationId());
        log.info(userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri());
        log.info(userRequest.getAccessToken().getTokenValue());

        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        // 🔹 사용자가 존재하지 않으면 자동 회원가입 진행
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setEmail(email);
            user.setNickname(name);
            userRepository.save(user);
        }

        return oauth2User;
    }
}
