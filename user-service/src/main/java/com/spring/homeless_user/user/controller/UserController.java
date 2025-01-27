package com.spring.homeless_user.user.controller;


import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @Autowired
    private final UserService userService;


    ////////////////////////////////////////////// 회원가입 및 정보처리 , 로그인 /////////////////////////////////////////////////////////////////
    //회원가입
    @PostMapping("/sign-up")
    public CommonResDto userSignUp(@ModelAttribute UserSaveReqDto dto) throws IOException {
        return userService.userSignUp(dto);
    }

    // 로그인
    @PostMapping("/sign-in")
    public CommonResDto userSignIn(@RequestBody UserLoginReqDto dto) {
        return userService.userSignIn(dto);
    }

    // 로그아웃
    @DeleteMapping("/sign-out")
    public CommonResDto userSignOut() {
        return userService.userSignOut();
    }

    // 토큰 갱신
    @PostMapping("/refresh-token")
    public CommonResDto reissueAccessToken(@RequestBody Map<String, String> data) {
        return userService.refreshToken(data.get("id"));
    }


    // 인증 이메일 전송 로직 인증번호 10분 유효 (이메일 만 필요)
    @PostMapping("/confirm")
    public CommonResDto sendVerificationEmail(@RequestBody EmailCheckDto dto) {
        return userService.sendVerificationEmail(dto);
    }


    // 이메일 인증번호 & 비밀번호 인증 확인
    @GetMapping("/confirm")
    public CommonResDto confirm(@ModelAttribute EmailCheckDto dto) {
        return userService.confirm(dto);
    }

    // 이메일 & 닉네임 중복검사 회원가입시
    @GetMapping("/duplicate")
    public CommonResDto duplicateCheck(@RequestParam(required = false) String email
            ,@RequestParam(required = false) String nickname) {
        return userService.duplicateCheck(email, nickname);
    }

    // 이메일 & 닉네임 중복검사 수정시
    @GetMapping("/duplicate/mod")
    public CommonResDto duplicateCheckmodify(@RequestParam(required = false) String email,
                                             @RequestParam(required = false) String nickname){
        return userService.duplicateCheck(email, nickname);
    }
    // 회원탈퇴
    @DeleteMapping("")
    public CommonResDto delete () {
        return userService.delete();
    }


    // 정보수정
    @PatchMapping( "")
    public CommonResDto modify(@ModelAttribute ModifyDto dto){
        return userService.modify(dto);
    }


    // 정보 조회
    @GetMapping("")
    public CommonResDto GetUserData(){
        return userService.getUserData();
    }


    // 유저데이터 모두 조회
    @GetMapping("/all")
    public CommonResDto allUser(){
        return userService.alluser();
    }

    ////////////////////////////////////////////////feign통신//////////////////////////////////////////////////////////
    @PostMapping("/details-by-email")
    public ResponseEntity<List> existsByNicknameAndRefreshToken(@RequestBody List<String> result) {
        log.info(result.toString());
        List<FeignResDto> dto1 = userService.existsByEmailAndRefreshToken(result);

        return ResponseEntity.ok(dto1);

    }

    @GetMapping("/get-email")
    public ResponseEntity<?> findEmailByNickname(@RequestParam("nickname") String nickname) {
        String findEmail = userService.changeEmail(nickname);
        return ResponseEntity.ok(findEmail);
    }

    /// feign 요청
    @PostMapping("/userList")
    public List<UserResponseDto> findByEmailIn(@RequestBody List<String> userEmails){

        List<UserResponseDto> byEmailIn = userService.findByEmailIn(userEmails);

        log.info("여기는 유저 컨트롤러 {}", byEmailIn);


        return byEmailIn;
    }

    @PostMapping("/friend")
    UserResponseDto findFriendByEmail(@RequestParam("email") String email) {
        return userService.findFriendByEmail(email);
    }

}


