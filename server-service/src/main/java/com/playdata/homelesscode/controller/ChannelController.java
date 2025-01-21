package com.playdata.homelesscode.controller;

import com.playdata.homelesscode.common.dto.CommonResDto;
import com.playdata.homelesscode.dto.channel.ChannelCreateDto;
import com.playdata.homelesscode.dto.channel.ChannelResponseDto;
import com.playdata.homelesscode.dto.channel.ChannelUpdateDto;
import com.playdata.homelesscode.entity.Channel;
import com.playdata.homelesscode.service.ChannelService;
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
@RequestMapping("/server")
public class ChannelController {


    private final ServerService serverService;
    private final ChannelService channelService;

    // 채널 생성
    @PostMapping("/channels")
    public ResponseEntity<?> createChannel(ChannelCreateDto dto) {

        Channel result = channelService.createChannel(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "채널 생성 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 채널 목록 조회
    @GetMapping("/channels")
    public ResponseEntity<?> getChannel(@RequestParam String id) {

        List<ChannelResponseDto> result = channelService.getChannel(id);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "조회 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 채널 삭제
    @DeleteMapping("/channels")
    public ResponseEntity<?> deleteChannel(@RequestParam String id) {

        channelService.deleteChannel(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 채널 수정
    @PutMapping("/channels")
    public ResponseEntity<?> updateChannel(ChannelUpdateDto dto) {
        Channel result = channelService.updateChannel(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "수정성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);

    }


}
