package com.playdata.homelesscode.service;

//import com.playdata.homelesscode.common.config.AwsS3Config;
import com.playdata.homelesscode.client.ChatServiceClient;
import com.playdata.homelesscode.client.UserServiceClient;
import com.playdata.homelesscode.common.utill.SecurityContextUtil;
import com.playdata.homelesscode.dto.boardList.BoardListCreateDto;
import com.playdata.homelesscode.dto.boardList.BoardListUpdateDto;
import com.playdata.homelesscode.dto.boards.BoardCreateDto;
import com.playdata.homelesscode.dto.channel.ChannelCreateDto;
import com.playdata.homelesscode.dto.channel.ChannelResponseDto;
import com.playdata.homelesscode.dto.channel.ChannelUpdateDto;
import com.playdata.homelesscode.dto.server.ServerCreateDto;
import com.playdata.homelesscode.dto.server.ServerResponseDto;
import com.playdata.homelesscode.entity.*;
import com.playdata.homelesscode.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ServerService {

    private final ServerRepository serverRepository;
    private final ServerJoinUserListRepository serverListRepository;
    private final ChannelRepository channelRepository;
    private final BoardListRepository boardListRepository;
    private final BoardRepository boardRepository;
//    private final AwsS3Config awsS3Config;
    private final SecurityContextUtil securityContextUtil;

    private final UserServiceClient userServiceClient;
    private final ChatServiceClient chatServiceClient;


    public Server createServer(ServerCreateDto dto) throws IOException {

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();

        log.info("이메일1111, {}", userEmail);
        Server server = dto.toEntity();
        server.setEmail(userEmail);
        server.setServerType(1);



        if(dto.getServerImg() != null){
            String fileName = UUID.randomUUID() + "-"  + dto.getServerImg().getOriginalFilename();

//        String imageUrl = awsS3Config.uploadToS3Bucket(dto.getServerImg().getBytes(), fileName);
            server.setServerImg(fileName);
        }



        Server result = serverRepository.save(server);


        log.info("이메일, {}", userEmail);

        ServerJoinUserList serverList = ServerJoinUserList.builder()
                .server(server)
                .email(userEmail)
                .role("Owner")
                .build();

        serverListRepository.save(serverList);


        return result;
    }

    public List<ServerResponseDto> getServer() {

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();

        List<ServerJoinUserList> byUserId = serverListRepository.findByEmail(userEmail);

        List<String> collect = byUserId.stream().map(s -> s.getServer().getId()).collect(Collectors.toList());

        List<Server> byIdIn = serverRepository.findByIdInOrServerType(collect, 0);





        List<ServerResponseDto> collect1 = byIdIn.stream().map(server -> {
            ServerJoinUserList serverJoinUserList = byUserId.stream().filter(s -> s.getServer().getId().equals(server.getId()))
                    .findFirst().orElse(null);
            return new ServerResponseDto(server.getId(),
                    server.getTag(),
                    server.getTitle(),
                    server.getServerImg(),
                    server.getEmail(), serverJoinUserList.getRole());
        }).collect(Collectors.toList());


        return collect1;

    }

    public void deleteServer(String id) {
        serverRepository.deleteById(id);
    }


    public void deleteServerList(String id) {

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();

        log.info("아이디 {}", id);
        log.info("이메일 {}", userEmail);


        serverListRepository.deleteByServerIdAndEmail(id, userEmail);


    }


    public Channel createChannel(ChannelCreateDto dto) {

        String serverId = dto.getServerId();

        Server server = serverRepository.findById(serverId).orElseThrow(() -> new NullPointerException("Server not found"));

        Channel channel = dto.toEntity(server);


        return channelRepository.save(channel);

    }

    public List<ChannelResponseDto> getChannel(String id) {

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();
        List<ServerJoinUserList> byEmail = serverListRepository.findByEmail(userEmail);

        List<ServerJoinUserList> collect = byEmail.stream().filter(s -> s.getServer().getId().equals(id)).collect(Collectors.toList());

        boolean checkRole = false;
        for (ServerJoinUserList server : collect) {
            String role = server.getRole(); // role 가져오기
            if ("Owner".equals(role) || "Manager".equals(role)) {
                checkRole = true;
                break; // 조건을 만족하는 role을 찾으면 더 이상 순회하지 않고 종료
            }
        }



        List<Channel> byServerId = channelRepository.findByServerId(id);
        List<ChannelResponseDto> list = byServerId.stream().map(c ->
                new ChannelResponseDto(
                        c.getId(),
                        c.getName(),
                        ChannelResponseDto.makeDateStringFomatter(c.getCreateAt()))
        ).toList();


        return list;

    }

    public void deleteChannel(String id) {

        channelRepository.deleteById(id);
    }

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

        board.setBoard(board);

        boardListRepository.save(board);

        return board;

    }

    public void deleteBoardList(String id) {

        boardListRepository.deleteById(id);

    }

    public List<BoardList> getBoardList(String id) {

        List<BoardList> board = boardListRepository.findByServerId(id);

        return board;

    }

    public Channel updateChannel(ChannelUpdateDto dto) {

        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow();

        channel.setName(dto.getName());


        Channel save = channelRepository.save(channel);

        return save;

    }


    public void createBoard(BoardCreateDto dto) {

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();

        BoardList boardList = boardListRepository.findById(dto.getBoardListId()).orElseThrow();
        //여기 이메일로 바꿔야됨
        Board board = Board.builder()
                .title(dto.getTitle())
                .writer(userEmail)
                .boardList(boardList)
                .build();

        boardRepository.save(board);

    }

    public List<Board> getBoard(String id) {

        List<Board> result = boardRepository.findByBoardListId(id);

        return result;

    }

    public void deleteChatMessageByChannelId(String channelId, @RequestHeader("Authorization") String authorization) {
        chatServiceClient.deleteChatMessageByChannelId(channelId, authorization);
    }
}
