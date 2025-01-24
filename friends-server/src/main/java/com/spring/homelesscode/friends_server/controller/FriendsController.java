package com.spring.homelesscode.friends_server.controller;


import com.spring.homelesscode.friends_server.common.utill.SecurityContextUtil;
import com.spring.homelesscode.friends_server.dto.CommonResDto;
import com.spring.homelesscode.friends_server.dto.FriendsDto;
import com.spring.homelesscode.friends_server.service.FriendsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendsController {

    private final FriendsService friendsAndServerService;
    private final SecurityContextUtil securityContextUtil;

    // 친구 요청
    @PostMapping("")
    public CommonResDto addFriend(@RequestBody FriendsDto dto) {
        log.info("addFriend, dto: {}", dto);
        return friendsAndServerService.addFriends(dto);
    }

    //친구목록 조회
    @GetMapping("")
    public CommonResDto userFriends() {
        log.info("userFriends");
        return friendsAndServerService.UserFriends();
    }

    // 친구삭제
    @DeleteMapping("")
    public CommonResDto deleteFriend(@RequestParam String receiverNickname) {
        log.info("deleteFriend");
        return friendsAndServerService.deleteFriend(receiverNickname);
    }

    //친구 요청응답
    @PostMapping("/response")
    public CommonResDto addResFriend(@RequestBody FriendsDto dto) {
        log.info("addFriend");
        log.info(dto.toString());
        return friendsAndServerService.addResFriends(dto);
    }

    // 친구 받은 목록 조회
    @GetMapping("/response")
    public CommonResDto resFriendJoin() {
        log.info("addFriendJoin");
        return friendsAndServerService.resFriendsJoin();
    }

    // 친구 요청한 목록
    @GetMapping("/request")
    public CommonResDto reqFriendJoin() {
        log.info("addFriendJoin");
        return friendsAndServerService.reqFriendsJoin();
    }

    // 친구요청 취소
    @DeleteMapping("/request")
    public CommonResDto reqFriendLeave(@RequestParam String receiverNickname) {
        log.info("addFriendLeave");
        return friendsAndServerService.deleteFriend(receiverNickname);
    }

}
