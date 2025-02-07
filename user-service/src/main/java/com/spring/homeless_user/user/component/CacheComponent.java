package com.spring.homeless_user.user.component;

import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CacheComponent {

    private final UserRepository userRepository;
    //캐싱 메서드
    @Cacheable(value = "userCache", key = "#email")
    public User getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    // 캐싱 수정
    @CachePut(value = "userCache", key = "#email")
    public User updateUserEntity(String email, User updatedUser) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        // 엔티티 수정
        existingUser.setNickname(updatedUser.getNickname());
        existingUser.setContents(updatedUser.getContents());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setProfileImage(updatedUser.getProfileImage());
        userRepository.save(existingUser);

        return existingUser; // 캐시에 저장됨
    }
}
