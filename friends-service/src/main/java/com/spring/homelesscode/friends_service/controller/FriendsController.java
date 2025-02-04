package com.spring.homelesscode.friends_service.controller;


import com.spring.homelesscode.friends_service.dto.CommonResDto;
import com.spring.homelesscode.friends_service.dto.FriendsDto;
import com.spring.homelesscode.friends_service.service.FriendsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendsController {

    private final FriendsService friendsService;

    // 친구 요청
    @PostMapping("")
    public CommonResDto addFriend(@RequestBody FriendsDto dto) {
        log.info("addFriend : {}", dto);
        return friendsService.addFriends(dto);
    }

    //친구목록 조회
    @GetMapping("")
    public CommonResDto getFriends() {
        return friendsService.getFriends();
    }

    // 친구삭제
    @DeleteMapping("")
    public CommonResDto deleteFriend(@RequestParam String receiverNickname) {
        return friendsService.deleteFriend(receiverNickname);
    }

    //친구 요청응답
    @PostMapping("/response")
    public CommonResDto addResFriend(@RequestBody FriendsDto dto) {
        return friendsService.addResFriends(dto);
    }

    // 친구 받은 목록 조회
    @GetMapping("/response")
    public CommonResDto resFriendJoin() {
        return friendsService.resFriendsJoin();
    }

    // 친구 요청한 목록
    @GetMapping("/request")
    public CommonResDto reqFriendJoin() {
        return friendsService.reqFriendsJoin();
    }

    // 친구요청 취소
    @DeleteMapping("/request")
    public CommonResDto reqFriendLeave(@RequestParam String receiverNickname) {
        return friendsService.deleteFriend(receiverNickname);
    }

}
