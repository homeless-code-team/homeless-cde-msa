package com.spring.homeless_user;

import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
class UserCreationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // PasswordEncoder 주입

    @Test
    @DisplayName("계정 생성 테스트")
    @Transactional
    @Rollback(false) // 테스트 후 데이터 유지
    void createUser() {

        String rawPassword = "1234"; // 원래 비밀번호
        String encodedPassword = passwordEncoder.encode(rawPassword); // 비밀번호 암호화
        // given: 테스트용 사용자 생성
        User user = User.builder()
                .email("test@1.com")
                .password(encodedPassword)
                .nickname("테스트유저입니다.")
                .profileImage("https://example.com/profile.png")
                .contents("안녕하세요! 저는 테스트 유저입니다.")
                .achievement("")
                .loginMethod("local")
                .createdAt(LocalDateTime.now())
                .refreshToken("")
                .build();

        // when: UserRepository를 사용해 사용자 저장
        User savedUser = userRepository.save(user);

        // then: 저장된 사용자 출력
        System.out.println("Saved User: " + savedUser);
    }
}
