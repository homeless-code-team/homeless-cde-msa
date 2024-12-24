package com.playdata.homelesscode.controller;

import com.playdata.homelesscode.common.dto.CommonResDto;
import com.playdata.homelesscode.dto.board.BoardCreateDto;
import com.playdata.homelesscode.dto.board.BoardUpdateDto;
import com.playdata.homelesscode.dto.channel.ChannelCreateDto;
import com.playdata.homelesscode.dto.channel.ChannelResponseDto;
import com.playdata.homelesscode.dto.server.ServerCreateDto;
import com.playdata.homelesscode.dto.server.ServerResponseDto;
import com.playdata.homelesscode.entity.Board;
import com.playdata.homelesscode.entity.Channel;
import com.playdata.homelesscode.entity.Server;
import com.playdata.homelesscode.service.ServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ServerController {


    private final ServerService serverService;

    @PutMapping("/server/create")
    public ResponseEntity<?> createServer(ServerCreateDto dto) {

        Server result = serverService.createServer(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "입력 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    @GetMapping("/server/get")
    public ResponseEntity<?> getServer() {
        String id = "3cc4dc0d-ca72-492f-9971-45e66a08f236";

        List<ServerResponseDto> result = serverService.getServer(id);


        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "서버 조회 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    @DeleteMapping("/server/delete")
    public ResponseEntity<?> deleteServer(@RequestParam("id") String id) {

        serverService.deleteServer(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PutMapping("/channel/create")
    public ResponseEntity<?> createChannel(ChannelCreateDto dto) {

        Channel result = serverService.createChannel(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "채널 생성 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    @GetMapping("/channel/get")
    public ResponseEntity<?> getChannel(@RequestParam String id) {

        List<ChannelResponseDto> result = serverService.getChannel(id);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "조회 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    @DeleteMapping("/channel/delete")
    public ResponseEntity<?> deleteChannel(@RequestParam String id) {

        serverService.deleteChannel(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/board/create")
    public ResponseEntity<?> createBoard(@RequestBody BoardCreateDto dto){
        Board result = serverService.createBoard(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "생성 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);

    }

    @PutMapping("/board/update")
    public ResponseEntity<?> updateBoard(@RequestBody BoardUpdateDto dto){

        Board board = serverService.updateBoard(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "수정 성공", board);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    @DeleteMapping("/board/delete")
    public ResponseEntity<?> deleteBoard(@RequestParam String id) {

        serverService.deleteBoard(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/board/get")
    public ResponseEntity<?> getBoard(@RequestParam String id) {

        List<Board> result = serverService.getBoard(id);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "조회 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

}
