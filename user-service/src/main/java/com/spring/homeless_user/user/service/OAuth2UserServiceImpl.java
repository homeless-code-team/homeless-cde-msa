package com.spring.homeless_user.user.service;

import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public OAuth2UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setNickname(name);
            userRepository.save(newUser);
        }

        return oauth2User;
    }
}
