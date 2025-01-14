package com.playdata.homelesscode.controller;

import com.playdata.homelesscode.common.dto.CommonResDto;
import com.playdata.homelesscode.dto.boardList.BoardListCreateDto;
import com.playdata.homelesscode.dto.boardList.BoardListUpdateDto;
import com.playdata.homelesscode.dto.boards.BoardCreateDto;
import com.playdata.homelesscode.dto.boards.BoardUpdateDto;
import com.playdata.homelesscode.dto.channel.ChannelCreateDto;
import com.playdata.homelesscode.dto.channel.ChannelResponseDto;
import com.playdata.homelesscode.dto.channel.ChannelUpdateDto;
import com.playdata.homelesscode.dto.server.ServerCreateDto;
import com.playdata.homelesscode.dto.server.ServerDto;
import com.playdata.homelesscode.dto.server.ServerResponseDto;
import com.playdata.homelesscode.entity.Board;
import com.playdata.homelesscode.entity.BoardList;
import com.playdata.homelesscode.entity.Channel;
import com.playdata.homelesscode.entity.Server;
import com.playdata.homelesscode.service.ServerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
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

    //서버 탍퇴
    @DeleteMapping("/serverList")
    public ResponseEntity<?> deleteServerList(@RequestParam("id") String id) {

        serverService.deleteServerList(id);


        return new ResponseEntity<>(HttpStatus.OK);
    }


    // 채널 생성
    @PostMapping("/channels")
    public ResponseEntity<?> createChannel(ChannelCreateDto dto) {

        Channel result = serverService.createChannel(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "채널 생성 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 채널 목록 조회
    @GetMapping("/channels")
    public ResponseEntity<?> getChannel(@RequestParam String id) {

        List<ChannelResponseDto> result = serverService.getChannel(id);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "조회 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 채널 삭제
    @DeleteMapping("/channels")
    public ResponseEntity<?> deleteChannel(@RequestParam String id, @RequestHeader("Authorization") String authorization) {

        serverService.deleteChannel(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 새널 수정
    @PutMapping("/channels")
    public ResponseEntity<?> updateChannel(ChannelUpdateDto dto) {
        Channel result = serverService.updateChannel(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "수정성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);

    }

    // 게시판 생성
    @PostMapping("/boardList")
    public ResponseEntity<?> createBoardList(BoardListCreateDto dto) {
        BoardList result = serverService.createBoardList(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "생성 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);

    }

    // 게시판 조회
    @GetMapping("/boardList")
    public ResponseEntity<?> getBoardList(@RequestParam String id) {

        List<BoardList> result = serverService.getBoardList(id);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "조회 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 게시판 수정
    @PutMapping("/boardList")
    public ResponseEntity<?> updateBoardList(BoardListUpdateDto dto) {

        BoardList board = serverService.updateBoardList(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "수정 성공", board);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 게시판 삭제
    @DeleteMapping("/boardList")
    public ResponseEntity<?> deleteBoardList(@RequestParam String id) {

        log.info("삭제 컨트롤러에여 {} ,", id);

        serverService.deleteBoardList(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 게시글 생성
    @PostMapping("/boards")
    public ResponseEntity<?> createBoards(BoardCreateDto dto) {

        serverService.createBoard(dto);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 게시글 조회
    @GetMapping("/boards")
    public ResponseEntity<?> getBoards(@RequestParam String id) {

        List<Board> result = serverService.getBoard(id);

        CommonResDto resDto = new CommonResDto<>(HttpStatus.OK, "조회 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    @DeleteMapping("/boards")
    public ResponseEntity<?> deleteBoards(@RequestParam String id) {

        serverService.deleteBoard(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PutMapping("/boards")
    public ResponseEntity<?> updateBoards(BoardUpdateDto dto) {

        serverService.updateBoard(dto);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    ////////////////////////////////////////////// 서버관리 /////////////////////////////////////////////////////////////////

    // 서버 추가요청
    @PostMapping("/servers/invite")
    public CommonResDto addReqServer(@RequestBody ServerDto dto) {
        log.info("addServer");
        return serverService.addReqServer(dto);
    }

    //서버 요청 응답
    @PostMapping("/servers/response")
    public CommonResDto addResServer(@RequestBody ServerDto dto) {
        log.info("addServerJoin");
        return serverService.addResServer(dto);
    }

    //서버추가 요청 조회
    @GetMapping("/servers/response")
    public CommonResDto addServerJoin(@RequestParam String serverId) {
        log.info("addServerJoin");
        return serverService.addServerJoin(serverId);
    }

}
