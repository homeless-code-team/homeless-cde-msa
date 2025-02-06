package com.spring.homeless_user.user.service;

import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FeignServiceTest {
    @Mock private UserRepository userRepository;
    private FeignService feignService;

    @BeforeEach
    void setUp() {
        feignService = new FeignService(userRepository);
    }

    @Test
    void 이메일로친구찾기_성공() {
        // given
        String email = "test@test.com";
        User user = new User();
        user.setEmail(email);
        user.setNickname("tester");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        var result = feignService.findFriendByEmail(email);

        // then
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getNickname()).isEqualTo("tester");
    }

    @Test
    void 여러이메일로친구찾기_성공() {
        // given
        List<String> emails = Arrays.asList("test1@test.com", "test2@test.com");
        User user1 = new User();
        user1.setEmail("test1@test.com");
        User user2 = new User();
        user2.setEmail("test2@test.com");

        when(userRepository.findByEmailIn(emails)).thenReturn(Arrays.asList(user1, user2));

        // when
        var result = feignService.findByEmailIn(emails);

        // then
        assertThat(result).hasSize(2);
    }
} 