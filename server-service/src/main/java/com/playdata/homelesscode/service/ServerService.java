package com.playdata.homelesscode.service;

import com.playdata.homelesscode.dto.board.BoardCreateDto;
import com.playdata.homelesscode.dto.board.BoardUpdateDto;
import com.playdata.homelesscode.dto.channel.ChannelCreateDto;
import com.playdata.homelesscode.dto.channel.ChannelResponseDto;
import com.playdata.homelesscode.dto.server.ServerCreateDto;
import com.playdata.homelesscode.dto.server.ServerResponseDto;
import com.playdata.homelesscode.entity.*;
import com.playdata.homelesscode.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerService {

    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final ServerListRepository serverListRepository;
    private final ChannelRepository channelRepository;
    private final BoardRepository boardRepository;

    public Server createServer(ServerCreateDto dto) {
        String userId = dto.getUserId();

        User user = userRepository.findById(userId).orElseThrow();

        Server server = dto.toEntity(user);

        Server result = serverRepository.save(server);

        ServerList serverList = ServerList.builder()
                .server(server)
                .user(user)
                .build();

        serverListRepository.save(serverList);


        return result;
    }

    public List<ServerResponseDto> getServer(String id) {

        List<ServerList> byUserId = serverListRepository.findByUserId(id);

        List<String> collect = byUserId.stream().map(s -> s.getServer().getId()).collect(Collectors.toList());

        List<Server> byIdIn = serverRepository.findByIdIn(collect);

        List<ServerResponseDto> collect1 = byIdIn.stream().map(e -> new ServerResponseDto(e.getId(), e.getTag(), e.getServerImg())).collect(Collectors.toList());

        return collect1;

    }

    public void deleteServer(String id) {
        serverRepository.deleteById(id);

    }

    public Channel createChannel(ChannelCreateDto dto) {

        String serverId = dto.getServerId();

        Server server = serverRepository.findById(serverId).orElseThrow(() -> new NullPointerException("Server not found"));

        Channel channel = dto.toEntity(server);


        return channelRepository.save(channel);

    }

    public List<ChannelResponseDto> getChannel(String id) {

        List<Channel> byServerId = channelRepository.findByServerId(id);
        List<ChannelResponseDto> list = byServerId.stream().map(c ->
                new ChannelResponseDto(
                        c.getName(),
                        ChannelResponseDto.makeDateStringFomatter(c.getCreateAt()))
        ).toList();


        return list;

    }

    public void deleteChannel(String id) {

        channelRepository.deleteById(id);


    }

    public Board createBoard(BoardCreateDto dto) {
        System.out.println(dto.getChannelId());
        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(() -> new NullPointerException("Channel not found"));

        Board board = dto.toEntity(channel);

        return boardRepository.save(board);

    }

    public Board updateBoard(BoardUpdateDto dto) {

        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow(() -> new NullPointerException("Channel not found"));

        Board board = boardRepository.findById(dto.getId()).orElseThrow();

        board.setBoard(board);

        boardRepository.save(board);

        return board;

    }

    public void deleteBoard(String id) {

        boardRepository.deleteById(id);

    }

    public List<Board> getBoard(String id) {

        List<Board> board = boardRepository.findByChannelId(id);

        return board;

    }
}
