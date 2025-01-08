package com.spring.homeless_user.user.controller;


import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @Autowired
    private final UserService userService;
    ////////////////////////////////////////////// 회원가입 및 정보처리 , 로그인 /////////////////////////////////////////////////////////////////
    //회원가입
    @PostMapping(value = "/sign-up")
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
        log.info("signin");
        return userService.userSignOut();
    }
    
    // 토큰 갱신
    @PostMapping("/refresh-token")
    public CommonResDto reissueAccessToken() {
        log.info("refresh token");
        return userService.refreshToken();
    }

//    @PostMapping("/git")
//    public CommonResDto gitLogin(@Valid @RequestBody UserLoginReqDto dto) {
//
//        return userService.gitLogin(dto);
//    }

    // 인증 이메일 전송 로직 인증번호 10분 유효 (이메일 만 필요)
    @PostMapping("/confirm")
    public CommonResDto sendVerificationEmail(@RequestBody EmailCheckDto dto) {
        log.info("confirm");
        return userService.sendVerificationEmail(dto);
    }


    // 이메일 인증번호 & 비밀번호 인증 확인
    @GetMapping("/confirm")
    public CommonResDto confirm(@RequestParam EmailCheckDto dto) {
    log.info("confirm");
        return userService.confirm(dto);
    }

    // 이메일 & 닉네임 중복검사
    @GetMapping("/duplicate")
    public CommonResDto duplicateCheck(@RequestParam DuplicateDto dto) throws IOException {
        log.info("duplicateCheck - email: {}, nickname: {}", dto.getEmail(), dto.getNickname());
        return userService.duplicateCheck(dto);
    }

    // 회원탈퇴
    @DeleteMapping("")
    public CommonResDto delete () {
        log.info("quit");
        return userService.delete();
    }


    // 정보수정
    @PatchMapping( "/")
    public CommonResDto modify(@ModelAttribute ModifyDto dto) throws IOException {
    log.info("profileUpdate");

        return userService.modify(dto);
    }

    // 정보 조회
    @GetMapping("/")
    public CommonResDto GetUserData(){
        log.info("getUserData");
        return userService.getUserData();
    }

}
