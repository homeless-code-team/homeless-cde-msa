package com.playdata.homelesscode.controller;


import com.playdata.homelesscode.common.dto.CommonResDto;
import com.playdata.homelesscode.dto.server.*;
import com.playdata.homelesscode.dto.user.UserReponseInRoleDto;
import com.playdata.homelesscode.entity.Server;
import com.playdata.homelesscode.service.ServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/server")
public class ServerController {


    private final ServerService serverService;


    // 서버 생성
    @PostMapping("/servers")
    public ResponseEntity<?> createServer(@ModelAttribute ServerCreateDto dto) throws IOException {

        Server result = serverService.createServer(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "입력 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 서버 조회
    @GetMapping("/servers")
    public ResponseEntity<?> getServer() {

        log.info("/server/servers: GET");
        List<ServerResponseDto> result = serverService.getServer();

        log.info("result: {}", result);
        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "서버 조회 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 서버 삭제
    @DeleteMapping("/servers")
    public ResponseEntity<?> deleteServer(@RequestParam("id") String id) {

        serverService.deleteServer(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    //서버 수정
    @PutMapping("/servers")
    public ResponseEntity<?> updateServer(@ModelAttribute ServerEditDto dto) throws IOException {

        serverService.editServer(dto);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    //서버 탍퇴
    @DeleteMapping("/serverList")
    public ResponseEntity<?> deleteServerList(@RequestParam("id") String id) {

        serverService.deleteServerList(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 서버 참여 유저 조회
    @GetMapping("/userList")
    public CommonResDto<List<UserReponseInRoleDto>> getUserList(@RequestParam String id) {


        List<UserReponseInRoleDto> userList = serverService.getUserList(id);
        CommonResDto<List<UserReponseInRoleDto>> List = new CommonResDto<>(HttpStatus.OK, "조회성공", userList);

        return List;

    }

    // 서버 권한 변경
    @PutMapping("/userRole")
    public ResponseEntity<?> changeRole(@RequestBody ChangeRoleDto dto) {
        serverService.changeRole(dto);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 유저 추방
    @DeleteMapping("/resign")
    public ResponseEntity<?> resignUser(@RequestBody ResignUserDto dto) {
        serverService.resignUser(dto);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 서버 초대 시 본인이 OWNER 혹은 MANAGER인 서버 조회
    @GetMapping("/serverList")
    public ResponseEntity<?> getServerList(){


        List<ServerResponseDto> serverList = serverService.getServerList();
        log.info("serverList in Controller: {}", serverList);


        CommonResDto<List<ServerResponseDto>> List = new CommonResDto<>(HttpStatus.OK, "조회성공", serverList);

        return new ResponseEntity<>(serverList,HttpStatus.OK);
    }

    ////////////////////////////////////////////// 서버관리 /////////////////////////////////////////////////////////////////

    // 서버 초대
    @PostMapping("/invite")
    public CommonResDto addReqServer(@RequestBody ServerDto dto) {
        log.info("addServer");
        return serverService.addReqServer(dto);
    }

    //서버 요청 응답
    @PostMapping("/acceptanceInvite")
    public CommonResDto addResServer(@RequestBody ServerDto dto) {
        log.info("addServerJoin");
        return serverService.addResServer(dto);
    }

    //서버추가 요청 조회
    @GetMapping("/inviteUserList")
    public CommonResDto addServerJoin(@RequestParam String serverId) {
        log.info("addServerJoin");
        return serverService.addServerJoin(serverId);
    }

    
    // 받은 요청
    @GetMapping("/inviteList")
    public CommonResDto<Void> inviteList(){

        return serverService.getInviteList();
    }

    @DeleteMapping("/cancelInvite")
    public CommonResDto<Void> cancelInvite(@RequestParam String serverId, @RequestParam String email) {

        return serverService.cancelInvite(serverId, email);
    }


}
