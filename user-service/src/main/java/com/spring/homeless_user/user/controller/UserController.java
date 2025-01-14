package com.spring.homeless_user.user.controller;


import com.spring.homeless_user.user.Oauth.OAuthService;
import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @Autowired
    private final UserService userService;

    private final OAuthService oAuthService;
    ////////////////////////////////////////////// 회원가입 및 정보처리 , 로그인 /////////////////////////////////////////////////////////////////
    //회원가입
    @PostMapping("/sign-up")
    public CommonResDto userSignUp(@ModelAttribute UserSaveReqDto dto) throws IOException {
        log.info("singup");
        return userService.userSignUp(dto);
    }

    // 로그인
    @PostMapping("/sign-in")
    public CommonResDto userSignIn(@RequestBody UserLoginReqDto dto) {
        log.info("signin");
        return userService.userSignIn(dto);
    }

    // 로그아웃
    @DeleteMapping("/sign-out")
    public CommonResDto userSignOut() {
        log.info("logout");
        return userService.userSignOut();
    }
    
    // 토큰 갱신
    @PostMapping("/refresh-token")
    public CommonResDto reissueAccessToken() {
        log.info("refresh token");
        return userService.refreshToken();
    }


    // 인증 이메일 전송 로직 인증번호 10분 유효 (이메일 만 필요)
    @PostMapping("/confirm")
    public CommonResDto sendVerificationEmail(@RequestBody EmailCheckDto dto) {
        log.info("confirm");
        return userService.sendVerificationEmail(dto);
    }


    // 이메일 인증번호 & 비밀번호 인증 확인
    @GetMapping("/confirm")
    public CommonResDto confirm(@ModelAttribute EmailCheckDto dto) {
    log.info("confirm, dto: {}", dto);
        return userService.confirm(dto);
    }

    // 이메일 & 닉네임 중복검사 회원가입시
    @GetMapping("/duplicate")
    public CommonResDto duplicateCheck(@RequestParam(required = false) String email
            ,@RequestParam(required = false) String nickname) throws IOException {
        log.info("duplicateCheck - email: {}, nickname: {}", email, nickname);
        return userService.duplicateCheck(email, nickname);
    }

    // 이메일 & 닉네임 중복검사 수정시
    @GetMapping("/duplicate/mod")
    public CommonResDto duplicateCheckmodify(@RequestParam(required = false) String email,
                                             @RequestParam(required = false) String nickname) throws IOException {
        log.info("duplicateCheck");
        return userService.duplicateCheck(email, nickname);
    }
    // 회원탈퇴
    @DeleteMapping("")
    public CommonResDto delete () {
        log.info("quit");
        return userService.delete();
    }


    // 정보수정
    @PatchMapping( "")
    public CommonResDto modify(@ModelAttribute ModifyDto dto) throws IOException {
    log.info("profileUpdate");
    log.info(String.valueOf(dto.getProfileImage()));

        return userService.modify(dto);
    }


    // 정보 조회
    @GetMapping("")
    public CommonResDto GetUserData(){
        log.info("getUserData");
        return userService.getUserData();
    }
    /////////////////////////////////////////////OAuth 2.0///////////////////////////////////////////////////////////////////
    // 1. 리다이렉션 URL 반환
    @GetMapping("/o-auth")
    public ResponseEntity<?> redirectToProvider(@RequestParam String provider) {
        log.info("/o-auth:GET, provider: {}", provider);
        if(provider == "google"){
            log.info("/o-auth:GET, provider: {}", provider);
            String redirectUrl = oAuthService.getGoogleRedirectUri();
        return ResponseEntity.ok().body(redirectUrl);}
        else{
            log.info("/o-auth:GET, provider: {}", provider);
            String redirectUrl = oAuthService.getGithubRedirectUri();
            return ResponseEntity.ok().body(redirectUrl);
        }
    }

    // 2. OAuth Callback 처리
    @PostMapping("/callback")
    public Mono<ResponseEntity<CommonResDto>> handleOAuthCallback(
            @RequestParam String code,
            @RequestParam String provider
    ) {
        return userService.getAccessToken(provider, code)
                .flatMap(accessToken -> userService.getUserInfo(provider, accessToken))
                .flatMap(userService::processOAuthUser)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(
                        new CommonResDto(null, 500, "OAuth 처리 중 오류 발생: " + e.getMessage(), null, null)
                )));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    @GetMapping("/exists-refresh-token")
    public ResponseEntity<Boolean> existsByNicknameAndRefreshToken(@RequestParam("nickname") String nickname) {
        boolean exists = userService.existsByNicknameAndRefreshToken(nickname);
        return ResponseEntity.ok(exists);
    }
}


