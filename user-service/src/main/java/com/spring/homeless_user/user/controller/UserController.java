package com.spring.homeless_user.user.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping(value = "/sign-up", consumes = "multipart/form-data")
    public CommonResDto userSignUp(
            @RequestPart("img") MultipartFile img,
            @RequestPart("data") String dataJson) throws IOException {
        log.info("singup");
        // form- data를 파싱해서 dto로 전환
        ObjectMapper objectMapper = new ObjectMapper();
        UserSaveReqDto dto = objectMapper.readValue(dataJson, UserSaveReqDto.class);

        return userService.userSignUp(dto, img);
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


    // 이메일 인증번호 & 비밀번호 인증 확인
    @GetMapping("/confirm")
    public CommonResDto confirm(@RequestParam String email, String token) {
    log.info("confirm");
        return userService.confirm(email, token);
    }

    // 이메일 & 닉네임 중복검사
    @GetMapping("/duplicate")
    public CommonResDto duplicateEmail(@RequestParam String email, String nickname) {
    log.info("duplicateEmail");
        return userService.duplicateCheck(email,nickname);
    }

    // 회원탈퇴
    @DeleteMapping("")
    public CommonResDto delete () {
        log.info("quit");
        return userService.delete();
    }


    // 정보수정
    @PatchMapping(value = "", consumes = "multipart/form-data")
    public CommonResDto modify(  @RequestPart("img") MultipartFile img,
                                 @RequestPart("data") String dataJson) throws IOException {
    log.info("profileImageUpdate");
        ObjectMapper objectMapper = new ObjectMapper();
        ModifyDto dto = objectMapper.readValue(dataJson, ModifyDto.class); {
        return userService.modify(dto, img);}
    }
////////////////////////////////////////////// 친구관리 /////////////////////////////////////////////////////////////////

    // 친구 요청
    @PostMapping("/friends")
    public CommonResDto addFriend(@RequestBody String resEmail) {
        log.info("addFriend");
        return userService.addFriends(resEmail);
    }

    //친구목록 조회
    @GetMapping("/friends")
    public CommonResDto userFriends() {
    log.info("userFriends");
        return userService.UserFriends();
    }

    // 친구삭제
    @DeleteMapping("/friends")
    public CommonResDto deleteFriend(@RequestBody String resEmail) {
        log.info("deleteFriend");
        return userService.deleteFriend(resEmail);
    }
        
    //친구 요청응답    
    @PostMapping("/friends/response")
    public CommonResDto addResFriend(@RequestBody friendsDto dto) {
        log.info("addFriend");
        return userService.addResFriends(dto);
    }

    // 친구 요청한 목록 및 친구 요청 받은  목록 조회
    @GetMapping("/friends/response")
        public CommonResDto addFriendJoin() {
        log.info("addFriendJoin");
            return userService.addFriendsJoin();
    }

    ////////////////////////////////////////////// 서버관리 /////////////////////////////////////////////////////////////////

    // 서버 추가요청
    @PostMapping("/server")
        public CommonResDto addReqServer(@RequestBody Long serverId){
        log.info("addFriend");
            return userService.addReqServer(serverId);
    }

    // 속한 서버 조회
    @GetMapping("/server")
        public CommonResDto userFriendJoin() {
        log.info("userFriends");
            return userService.userServerJoin();
    }

    // 서버 탈퇴
    @DeleteMapping("/server")
        public CommonResDto deleteFriend(@RequestParam long serverId) {
        log.info("deleteFriend");
            return userService.deleteServer(serverId);
    }

    //서버 요청 응답
    @PostMapping("/server/response")
        public CommonResDto addResServer(@RequestBody long serverId, String status) {
        log.info("addFriend");
                return userService.addResServer(serverId, status);
    }

    //서버추가 요청 조회
    @GetMapping("/server/response")
       public CommonResDto addServerJoin() {
        log.info("addFriendJoin");
                return userService.addServerJoin();
    }

}
