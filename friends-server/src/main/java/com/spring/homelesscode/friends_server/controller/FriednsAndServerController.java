package com.spring.homelesscode.friends_server.controller;


import com.spring.homelesscode.friends_server.dto.CommonResDto;
import com.spring.homelesscode.friends_server.dto.FriendsDto;
import com.spring.homelesscode.friends_server.service.FriendsAndServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/friends")
@RequiredArgsConstructor
@Slf4j
public class FriednsAndServerController {

    private final FriendsAndServerService friendsAndServerService;

    // 친구 요청
    @PostMapping("")
    public CommonResDto addFriend(@RequestBody FriendsDto dto) {
        log.info("addFriend");
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
    public CommonResDto deleteFriend(@RequestBody FriendsDto dto ) {
        log.info("deleteFriend");
        return friendsAndServerService.deleteFriend(dto);
    }

    //친구 요청응답
    @PostMapping("/response")
    public CommonResDto addResFriend(@RequestBody FriendsDto dto) {
        log.info("addFriend");
        log.info(dto.toString());
        return friendsAndServerService.addResFriends(dto);
    }

    // 친구 요청한 목록 및 친구 요청 받은  목록 조회
    @GetMapping("/response")
    public CommonResDto resFriendJoin() {
        log.info("addFriendJoin");
        return friendsAndServerService.resFriendsJoin();
    }

    // 친구 요청한 목록 및 친구 요청 받은  목록 조회
    @GetMapping("/request")
    public CommonResDto reqFriendJoin() {
        log.info("addFriendJoin");
        return friendsAndServerService.reqFriendsJoin();
    }

    // 친구 요청한 목록 및 친구 요청 받은  목록 조회
    @DeleteMapping("/request")
    public CommonResDto reqFriendDelete(@RequestBody FriendsDto dto ) {
        log.info("addFriendDelete");
        return friendsAndServerService.reqFriendsDelete(dto);
    }

    ////////////////////////////////////////////// 서버관리 /////////////////////////////////////////////////////////////////
//
//    // 서버 추가요청
//    @PostMapping("/servers")
//    public CommonResDto addReqServer(@RequestBody ServerDto dto){
//        log.info("addServer");
//        return friendsAndServerService.addReqServer(dto);
//    }
//
//    //서버 요청 응답
//    @PostMapping("/servers/response")
//    public CommonResDto addResServer(@RequestBody ServerDto dto) {
//        log.info("addServerJoin");
//        return friendsAndServerService.addResServer(dto);
//    }
//
//    //서버추가 요청 조회
//    @GetMapping("/servers/response")
//    public CommonResDto addServerJoin(@RequestParam String serverId) {
//        log.info("addServerJoin");
//        return friendsAndServerService.addServerJoin(serverId);
//    }

}
