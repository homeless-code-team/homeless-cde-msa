package com.playdata.homelesscode.controller;

import com.playdata.homelesscode.common.dto.CommonResDto;
import com.playdata.homelesscode.dto.boardList.BoardListCreateDto;
import com.playdata.homelesscode.dto.boardList.BoardListUpdateDto;
import com.playdata.homelesscode.entity.BoardList;
import com.playdata.homelesscode.service.BoardListService;
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
public class BoardListController {

    private final BoardListService boardListService;

    // 게시판 생성
    @PostMapping("/boardList")
    public ResponseEntity<?> createBoardList(BoardListCreateDto dto) {
        BoardList result = boardListService.createBoardList(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "생성 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);

    }

    // 게시판 조회
    @GetMapping("/boardList")
    public ResponseEntity<?> getBoardList(@RequestParam String id) {

        log.info("아이디 : {}", id);

        List<BoardList> result = boardListService.getBoardList(id);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "조회 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 게시판 수정
    @PutMapping("/boardList")
    public ResponseEntity<?> updateBoardList(BoardListUpdateDto dto) {

        BoardList board = boardListService.updateBoardList(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "수정 성공", board);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 게시판 삭제
    @DeleteMapping("/boardList")
    public ResponseEntity<?> deleteBoardList(@RequestParam String id) {

        log.info("삭제 컨트롤러에여 {} ,", id);

        boardListService.deleteBoardList(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }


}
