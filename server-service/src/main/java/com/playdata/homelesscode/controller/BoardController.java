package com.playdata.homelesscode.controller;

import com.playdata.homelesscode.common.dto.CommonResDto;
import com.playdata.homelesscode.dto.boards.BoardCreateDto;
import com.playdata.homelesscode.dto.boards.BoardDeleteDto;
import com.playdata.homelesscode.dto.boards.BoardSearchDto;
import com.playdata.homelesscode.dto.boards.BoardUpdateDto;
import com.playdata.homelesscode.entity.Board;
import com.playdata.homelesscode.service.ServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/server")
public class BoardController {

    private final ServerService serverService;
    // 게시글 생성
    @PostMapping("/boards")
    public ResponseEntity<?> createBoards(BoardCreateDto dto) {
        try {
            serverService.createBoard(dto);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalStateException e) {
            // 이미 삭제된 게시판인 경우
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            // 기타 에러 처리
            return new ResponseEntity<>("게시판 생성 중 문제가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 게시글 조회
    @GetMapping("/boards")
    public ResponseEntity<?> getBoards(BoardSearchDto dto, Pageable pageable) {

        Page<Board> result = serverService.getBoard(dto,pageable);

        CommonResDto resDto = new CommonResDto<>(HttpStatus.OK, "조회 성공", result);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 게시글 삭제
    @DeleteMapping("/boards")
    public ResponseEntity<?> deleteBoards(BoardDeleteDto dto) {

        serverService.deleteBoard(dto);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 게시글 수정
    @PutMapping("/boards")
    public ResponseEntity<?> updateBoards(BoardUpdateDto dto) {

        serverService.updateBoard(dto);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
