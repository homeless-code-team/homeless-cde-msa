package com.spring.homeless_user.user.service;

import com.spring.homeless_user.user.dto.EmailCheckDto;
import com.spring.homeless_user.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CheckServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private RedisTemplate<String, String> checkTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    private CheckService checkService;

    @BeforeEach
    void setUp() {
        checkService = new CheckService(null, null, userRepository, checkTemplate);
        when(checkTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 이메일중복체크_성공() {
        // given
        String email = "test@test.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // when
        var result = checkService.checkEmail(email);

        // then
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getHttpStatus().value()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("사용 가능한 이메일입니다.");
    }

    @Test
    void 닉네임중복체크_성공() {
        // given
        String nickname = "tester";
        when(userRepository.existsByNickname(nickname)).thenReturn(false);

        // when
        var result = checkService.checkNickname(nickname);

        // then
        assertThat(result.getHttpStatus().value()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("사용 가능한 닉네임입니다.");
    }
} 