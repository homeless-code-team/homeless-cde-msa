package com.playdata.homelesscode.service;

import com.playdata.homelesscode.common.custom.CustomThrowException;
import com.playdata.homelesscode.common.utill.SecurityContextUtil;
import com.playdata.homelesscode.dto.boardList.BoardListCreateDto;
import com.playdata.homelesscode.dto.boardList.BoardListUpdateDto;
import com.playdata.homelesscode.dto.server.Role;
import com.playdata.homelesscode.entity.BoardList;
import com.playdata.homelesscode.entity.Server;
import com.playdata.homelesscode.entity.ServerJoinUserList;
import com.playdata.homelesscode.repository.BoardListRepository;
import com.playdata.homelesscode.repository.ServerJoinUserListRepository;
import com.playdata.homelesscode.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardListService {


    private final BoardListRepository boardListRepository;
    private final ServerRepository serverRepository;
    private final ServerJoinUserListRepository serverListRepository;



    public BoardList createBoardList(BoardListCreateDto dto) {
        System.out.println(dto.getServerId());

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();

        Server server = serverRepository.findById(dto.getServerId()).orElseThrow();

        BoardList board = dto.toEntity(server);


        //여기 이메일로 바꿔야됨
        board.setWriter(userEmail);

        return boardListRepository.save(board);

    }

    public BoardList updateBoardList(BoardListUpdateDto dto) {

        Server server = serverRepository.findById(dto.getServerId()).orElseThrow(() -> new NullPointerException("server not found"));

        BoardList board = boardListRepository.findById(dto.getId()).orElseThrow();

        board.setBoardTitle(dto.getBoardTitle());
        board.setTag(dto.getTag());

        boardListRepository.save(board);

        return board;

    }

    public void deleteBoardList(String id) {

        String email = SecurityContextUtil.getCurrentUser().getEmail();

        BoardList boardList = boardListRepository.findById(id).orElseThrow();

        ServerJoinUserList serverList = serverListRepository.findByEmailAndServerId(email, boardList.getServer().getId());


        if (serverList.getRole() == Role.OWNER || serverList.getRole() == Role.MANAGER) {
            boardListRepository.deleteById(id);
        }else {
            throw new CustomThrowException("권한 부족");
        }





    }

    public List<BoardList> getBoardList(String id) {

        List<BoardList> board = boardListRepository.findByServerId(id);

        return board;

    }

}
