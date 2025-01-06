package com.spring.homeless_user.user.controller;

import com.spring.homeless_user.user.dto.CommonResDto;
import com.spring.homeless_user.user.dto.FriendsDto;
import com.spring.homeless_user.user.dto.ServerDto;
import com.spring.homeless_user.user.service.FriendsAndServerService;
import com.spring.homeless_user.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class FriednsAndServerController {

    private final FriendsAndServerService friendsAndServerService;

    // 친구 요청
    @PostMapping("/friends")
    public CommonResDto addFriend(@RequestBody FriendsDto dto) {
        log.info("addFriend");
        return friendsAndServerService.addFriends(dto);
    }

    //친구목록 조회
    @GetMapping("/friends")
    public CommonResDto userFriends() {
        log.info("userFriends");
        return friendsAndServerService.UserFriends();
    }

    // 친구삭제
    @DeleteMapping("/friends")
    public CommonResDto deleteFriend(@RequestBody FriendsDto dto ) {
        log.info("deleteFriend");
        return friendsAndServerService.deleteFriend(dto);
    }

    //친구 요청응답
    @PostMapping("/friends/response")
    public CommonResDto addResFriend(@RequestBody FriendsDto dto) {
        log.info("addFriend");
        log.info(dto.toString());
        return friendsAndServerService.addResFriends(dto);
    }

    // 친구 요청한 목록 및 친구 요청 받은  목록 조회
    @GetMapping("/friends/response")
    public CommonResDto addFriendJoin() {
        log.info("addFriendJoin");
        return friendsAndServerService.addFriendsJoin();
    }

    ////////////////////////////////////////////// 서버관리 /////////////////////////////////////////////////////////////////

    // 서버 추가요청
    @PostMapping("/servers")
    public CommonResDto addReqServer(@RequestBody ServerDto dto){
        log.info("addFriend");
        return friendsAndServerService.addReqServer(dto);
    }

    //서버 요청 응답
    @PostMapping("/servers/response")
    public CommonResDto addResServer(@RequestBody ServerDto dto) {
        log.info("addFriend");
        return friendsAndServerService.addResServer(dto);
    }

    //서버추가 요청 조회
    @GetMapping("/servers/response")
    public CommonResDto addServerJoin(@RequestParam String serverId) {
        log.info("addFriendJoin");
        return friendsAndServerService.addServerJoin(serverId);
    }

}
