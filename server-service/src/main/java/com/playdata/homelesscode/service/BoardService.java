package com.playdata.homelesscode.service;

import com.playdata.homelesscode.client.ChatServiceClient;
import com.playdata.homelesscode.common.utill.SecurityContextUtil;
import com.playdata.homelesscode.dto.boards.BoardCreateDto;
import com.playdata.homelesscode.dto.boards.BoardDeleteDto;
import com.playdata.homelesscode.dto.boards.BoardSearchDto;
import com.playdata.homelesscode.dto.boards.BoardUpdateDto;
import com.playdata.homelesscode.dto.server.Role;
import com.playdata.homelesscode.entity.Board;
import com.playdata.homelesscode.entity.BoardList;
import com.playdata.homelesscode.entity.ServerJoinUserList;
import com.playdata.homelesscode.repository.BoardListRepository;
import com.playdata.homelesscode.repository.BoardRepository;
import com.playdata.homelesscode.repository.ServerJoinUserListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardListRepository boardListRepository;
    private final ServerJoinUserListRepository serverListRepository;
    private final ChatServiceClient chatServiceClient;


    public Board createBoard(BoardCreateDto dto) {


        String userNickName = SecurityContextUtil.getCurrentUser().getNickname();

        BoardList boardList = boardListRepository.findById(dto.getBoardListId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 게시판이 존재하지 않습니다."));

        Board board = Board.builder()
                .title(dto.getTitle())
                .writer(userNickName)
                .boardList(boardList)
                .build();

        boardRepository.save(board);
        return board;
    }


    public Page<Board> getBoard(BoardSearchDto dto, Pageable pageable) {

//        List<Board> result = boardRepository.findByBoardListIdOrderByCreateAtDesc(dto.getId(), pageable);
//        return result;

        if (dto.getSearchName() != null) {
            log.info("아이디 {}", dto.getId());
            log.info("검색 {}", dto.getSearchName());
            Page<Board> result = boardRepository.findByBoardListIdAndTitleContainingOrderByCreateAtDesc(dto.getId(), dto.getSearchName(), pageable);
            return result;
        } else {
            log.info("널인데?");
            Page<Board> result = boardRepository.findByBoardListIdOrderByCreateAtDesc(dto.getId(), pageable);
            return result;
        }


    }

    public void deleteBoard(BoardDeleteDto dto) {

        String email = SecurityContextUtil.getCurrentUser().getEmail();

        ServerJoinUserList byEmailAndServerId = serverListRepository.findByEmailAndServerId(email, dto.getServeId());

        Board board = boardRepository.findById(dto.getBoardId()).orElseThrow();

        if (board.getWriter().equals(email) || byEmailAndServerId.getRole() == Role.OWNER || byEmailAndServerId.getRole() == Role.MANAGER) {
            chatServiceClient.deleteChatMessageByChannelId(dto.getBoardId());
            boardRepository.deleteById(dto.getBoardId());
        }


    }

    public void updateBoard(BoardUpdateDto dto) {

        Board board = boardRepository.findById(dto.getBoardId()).orElseThrow();

        board.setTitle(dto.getBoardTitle());

        boardRepository.save(board);
    }


}
