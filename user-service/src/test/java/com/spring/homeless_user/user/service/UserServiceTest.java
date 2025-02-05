package com.spring.homeless_user.user.service;

import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.user.component.CacheComponent;
import com.spring.homeless_user.user.dto.UserLoginReqDto;
import com.spring.homeless_user.user.dto.UserSaveReqDto;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private CacheComponent cacheComponent;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, jwtTokenProvider,
                null, cacheComponent, null, null);
    }

    @Test
    void 회원가입_성공() {
        // given
        UserSaveReqDto dto = new UserSaveReqDto();
        dto.setEmail("test@test.com");
        dto.setPassword("Test1234!");
        dto.setNickname("tester");

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(new User());

        // when
        var result = userService.userSignUp(dto);

        // then
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("회원가입을 환영합니다.");
    }

    @Test
    void 로그인_성공() {
        // given
        UserLoginReqDto dto = new UserLoginReqDto();
        dto.setEmail("test@test.com");
        dto.setPassword("Test1234!");

        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");

        when(cacheComponent.getUserEntity(any())).thenReturn(user);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtTokenProvider.refreshToken(any(), any())).thenReturn("refreshToken");
        when(jwtTokenProvider.accessToken(any(), any(), any())).thenReturn("accessToken");

        // when
        var result = userService.userSignIn(dto);

        // then
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo("accessToken");
    }
} 