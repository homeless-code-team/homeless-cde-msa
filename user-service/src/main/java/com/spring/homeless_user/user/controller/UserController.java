package com.spring.homeless_user.user.controller;


import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.service.CheckService;
import com.spring.homeless_user.user.service.FeignService;
import com.spring.homeless_user.user.service.InfomationService;
import com.spring.homeless_user.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    private final UserService userService;
    private final CheckService checkService;
    private final InfomationService infomationService;
    private final FeignService feignService;




    //////////////////////////////////////////////
    // 회원가입, 로그인, 토큰 관련 API
    //////////////////////////////////////////////

    // ✅ 회원가입
    @PostMapping("/sign-up")
    public ResponseEntity<CommonResDto> userSignUp(@ModelAttribute UserSaveReqDto dto) {
        CommonResDto response = userService.userSignUp(dto);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // ✅ 로그인
    @PostMapping("/sign-in")
    public ResponseEntity<CommonResDto> userSignIn(@RequestBody UserLoginReqDto dto) {
        CommonResDto response = userService.userSignIn(dto);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // ✅ 로그아웃
    @DeleteMapping("/sign-out")
    public ResponseEntity<CommonResDto> userSignOut() {
        CommonResDto response = userService.userSignOut();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // ✅ 토큰 갱신
    @PostMapping("/refresh-token")
    public ResponseEntity<CommonResDto> reissueAccessToken(@RequestBody Map<String, String> data) {
        CommonResDto response = userService.refreshToken(data.get("id"));
        return ResponseEntity.status(response.getCode()).body(response);
    }

    //////////////////////////////////////////////
    // 이메일 인증 관련 API
    //////////////////////////////////////////////

    // ✅ 인증 이메일 전송 (인증번호 10분 유효)
    @PostMapping("/confirm")
    public ResponseEntity<CommonResDto> sendVerificationEmail(@RequestBody EmailCheckDto dto) {
        CommonResDto response = checkService.sendVerificationEmail(dto);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // ✅ 이메일 인증번호 확인
    @GetMapping("/confirm")
    public ResponseEntity<CommonResDto> confirm(@ModelAttribute EmailCheckDto dto) {
        CommonResDto response = checkService.confirm(dto);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    //////////////////////////////////////////////
    // 중복 검사 관련 API
    //////////////////////////////////////////////

    // ✅ 회원가입 시 이메일 & 닉네임 중복검사
    @GetMapping("/duplicate")
    public ResponseEntity<CommonResDto> duplicateCheck(@RequestParam(required = false) String email,
                                                       @RequestParam(required = false) String nickname) {
        CommonResDto response = checkService.duplicateCheck(email, nickname);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // ✅ 수정 시 이메일 & 닉네임 중복검사
    @GetMapping("/duplicate/mod")
    public ResponseEntity<CommonResDto> duplicateCheckModify(@RequestParam(required = false) String email,
                                                             @RequestParam(required = false) String nickname) {
        CommonResDto response = checkService.duplicateCheck(email, nickname);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    //////////////////////////////////////////////
    // 회원 정보 관리 API
    //////////////////////////////////////////////

    // ✅ 회원 탈퇴
    @DeleteMapping("")
    public ResponseEntity<CommonResDto> delete() {
        CommonResDto response = userService.delete();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // ✅ 회원 정보 수정
    @PatchMapping("")
    public ResponseEntity<CommonResDto> modify(@ModelAttribute ModifyDto dto) {
        CommonResDto response = infomationService.modify(dto);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // ✅ 내 정보 조회
    @GetMapping("")
    public ResponseEntity<CommonResDto> getUserData() {
        CommonResDto response = infomationService.getUserData();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // ✅ 모든 유저 정보 조회
    @GetMapping("/all")
    public ResponseEntity<CommonResDto> allUsers() {
        CommonResDto response = infomationService.alluser();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // ✅ 특정 유저 정보 조회
    @GetMapping("/get")
    public ResponseEntity<CommonResDto> getUserByNickname(@RequestParam(required = false) String nickname) {
        CommonResDto response = infomationService.getData(nickname);
        return ResponseEntity.status(response.getCode()).body(response);
    }
    ////////////////////////////////////////////////feign통신//////////////////////////////////////////////////////////
    @PostMapping("/details-by-email")
    public ResponseEntity<List> existsByNicknameAndRefreshToken(@RequestBody List<String> result) {
        List<FeignResDto> dto1 = feignService.existsByEmailAndRefreshToken(result);

        return ResponseEntity.ok(dto1);

    }

    @GetMapping("/get-email")
    public ResponseEntity<?> findEmailByNickname(@RequestParam("nickname") String nickname) {
        String findEmail = feignService.changeEmail(nickname);
        return ResponseEntity.ok(findEmail);
    }


    @PostMapping("/userList")
    public List<UserResponseDto> findByEmailIn(@RequestBody List<String> userEmails){

        List<UserResponseDto> byEmailIn = feignService.findByEmailIn(userEmails);
        return byEmailIn;
    }

    @PostMapping("/friend")
    UserResponseDto findFriendByEmail(@RequestParam("email") String email) {
        return feignService.findFriendByEmail(email);
    }

}
