package com.spring.homeless_user.user.service;

import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.common.utill.JwtUtil;
import com.spring.homeless_user.common.utill.SecurityContextUtil;
import com.spring.homeless_user.user.component.CacheComponent;
import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.entity.Provider;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;
    private final CacheComponent cacheComponent;
    private final RedisTemplate<String, String> loginTemplate;
    private final RedisTemplate<String, String> cacheTemplate;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JwtUtil jwtUtil, CacheComponent cacheComponent,
                       @Qualifier("login") RedisTemplate<String, String> loginTemplate,
                       @Qualifier("cache") RedisTemplate<String, String> cacheTemplate
                       ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtUtil = jwtUtil;
        this.cacheComponent = cacheComponent;
        this.loginTemplate = loginTemplate;
        this.cacheTemplate = cacheTemplate;
    }

    //회원가입 로직
    public CommonResDto userSignUp(UserSaveReqDto dto) {

        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sign-up", "/api/v1/users/sign-up", "POST"));
        links.add(new CommonResDto.Link("login", "/api/v1/users/sign-in", "POST"));
        links.add(new CommonResDto.Link("profileModify", "/api/v1/users", "PATCH"));
        links.add(new CommonResDto.Link("logout", "/api/v1/users/logout", "POST"));
        links.add(new CommonResDto.Link("Delete", "/api/v1/users", "DELETE"));

        log.info(String.valueOf(dto));

        try {

            User user = new User();
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setNickname(dto.getNickname());
            user.setProvider(Provider.LOCAL);
            user.setCreatedAt(LocalDateTime.now());

            userRepository.save(user);

            return new CommonResDto(HttpStatus.OK, 200, "회원가입을 환영합니다.", null, links);

        } catch (Exception e) {
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 오류: " + e.getMessage(), null, links);
        }
    }

    public CommonResDto userSignIn(@Valid UserLoginReqDto dto) {

        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("login", "/api/v1/users/sign-in", "POST"));
        links.add(new CommonResDto.Link("profileModify", "/api/v1/users", "PATCH"));
        links.add(new CommonResDto.Link("logout", "/api/v1/users/logout", "POST"));
        links.add(new CommonResDto.Link("Delete", "/api/v1/users", "DELETE"));


        try {
            String email = dto.getEmail();
            User user = cacheComponent.getUserEntity(email);

            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                return new CommonResDto(HttpStatus.UNAUTHORIZED, 401, "잘못된 비밀번호입니다.", null, links);
            }

            String refreshToken = jwtTokenProvider.refreshToken(email, user.getId());
            String accessToken = jwtTokenProvider.accessToken(email, user.getId(), user.getNickname());

            loginTemplate.opsForValue().set(dto.getEmail(), refreshToken, 14, TimeUnit.DAYS);

            return new CommonResDto(HttpStatus.OK, 200, "SignIn successfully.", accessToken, links);
        } catch (Exception e) {
             e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "로그인 처리 중 오류 발생: " + e.getMessage(), null, links);
        }
    }

    //로그아웃
    public CommonResDto userSignOut() {
        // REST API 링크 설정
        CommonResDto.Link Link = new CommonResDto.Link("logout", "api/v1/users/sign-out", "DELETE");

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            loginTemplate.delete(email);
            cacheTemplate.delete(email);

            return new CommonResDto(HttpStatus.OK, 200, "SignOut successfully.", null, List.of(Link));
        } catch (Exception e) {
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "로그아웃중 에러 발생", e.getMessage(), List.of(Link));
        }

    }

    //토큰갱신
    public CommonResDto refreshToken(String id) {

        // REST API 링크 설정
        CommonResDto.Link Link = new CommonResDto.Link("TokenRefresh", "api/v1/users/refresh", "POST");

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + id));
            String refreshToken = loginTemplate.opsForValue().get(user.getEmail());
            boolean flag = jwtUtil.isTokenExpired(refreshToken);

            if (!flag) {
                String newAccessToken = jwtTokenProvider.accessToken(user.getEmail(), id, user.getNickname());
                loginTemplate.delete(user.getEmail());
                return new CommonResDto(HttpStatus.OK, 200, "Refresh token successfully.", newAccessToken, List.of(Link));
            } else {
                return new CommonResDto(HttpStatus.UNAUTHORIZED, 401, "유효하지 않은 리프레시 토큰입니다.", null, List.of(Link));
            }
        } catch (Exception e) {
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "토큰 갱신 중 오류 발생: " + e.getMessage(), null, List.of(Link));
        }
    }


    //회원탈퇴
    public CommonResDto delete() {
        // REST API 링크 설정
        CommonResDto.Link Link = new CommonResDto.Link("Delete", "api/v1/users", "DELETE");
        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            User user = cacheComponent.getUserEntity(email);

            userRepository.delete(user);
            loginTemplate.delete(email);
            cacheTemplate.delete(email);

            return new CommonResDto(HttpStatus.OK, 200, "삭제완료", null, List.of(Link));
        } catch (Exception e) {
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "에러발생" + e.getMessage(), null, List.of(Link));
        }
    }
//    public void registerOrLoginOAuthUser(String email, String nickname) {
//        // 이미 가입된 회원인지 확인
//        if (!userRepository.existsByEmail(email)) {
//            // 새로운 회원이면 회원가입 처리
//            User user = User.builder()
//                    .email(email)
//                    .nickname(nickname)
//                    .provider(Provider.SOCIALLOGIN)
//                    .createdAt(LocalDateTime.now())
//                    .build();
//            userRepository.save(user);
//        }
//    }
}
