package com.spring.homeless_user.user.service;

import com.spring.homeless_user.user.component.CacheComponent;
import com.spring.homeless_user.user.dto.ModifyDto;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InfomationServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private CacheComponent cacheComponent;
    @Mock private PasswordEncoder passwordEncoder;
    private InfomationService infomationService;

    @BeforeEach
    void setUp() {
        infomationService = new InfomationService(userRepository, cacheComponent, passwordEncoder, null);
    }

    @Test
    void 사용자정보조회_성공() {
        // given
        String nickname = "tester";
        User user = new User();
        user.setNickname(nickname);
        user.setContents("test contents");
        
        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));

        // when
        var result = infomationService.getData(nickname);

        // then
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("조회완료");
    }

    @Test
    void 닉네임변경_성공() {
        // given
        ModifyDto dto = new ModifyDto();
        dto.setNickname("newNickname");
        
        User user = new User();
        user.setNickname("oldNickname");

        when(cacheComponent.getUserEntity(any())).thenReturn(user);
        when(userRepository.findByNickname(dto.getNickname())).thenReturn(Optional.empty());

        // when
        var result = infomationService.modify(dto);

        // then
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("닉네임변경성공");
    }
} 