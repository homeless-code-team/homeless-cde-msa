package com.spring.homeless_user.common.auth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections; 
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, user);
        } catch (Exception ex) {
            log.error("OAuth2 사용자 처리 중 오류 발생", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = null;
        String name = null;
        
        log.info("OAuth 제공자: {}", registrationId);
        log.info("원본 속성: {}", attributes);

        // 제공자별 속성 매핑
        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else if ("github".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("login");  // GitHub는 login을 사용자 이름으로 사용
        }
        
        log.info("매핑된 이메일: {}, 이름: {}", email, name);
        
        // 사용자 정보를 attributes에 추가
        Map<String, Object> mappedAttributes = new HashMap<>(attributes);
        mappedAttributes.put("email", email);
        mappedAttributes.put("name", name);
        
        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            mappedAttributes,
            "email"  // nameAttributeKey
        );
    }
} 