package com.playdata.homelesscode.service;

import com.playdata.homelesscode.client.ChatServiceClient;
import com.playdata.homelesscode.common.custom.CustomThrowException;
import com.playdata.homelesscode.common.utill.SecurityContextUtil;
import com.playdata.homelesscode.dto.channel.ChannelCreateDto;
import com.playdata.homelesscode.dto.channel.ChannelResponseDto;
import com.playdata.homelesscode.dto.channel.ChannelUpdateDto;
import com.playdata.homelesscode.dto.server.Role;
import com.playdata.homelesscode.entity.Channel;
import com.playdata.homelesscode.entity.Server;
import com.playdata.homelesscode.entity.ServerJoinUserList;
import com.playdata.homelesscode.repository.ChannelRepository;
import com.playdata.homelesscode.repository.ServerJoinUserListRepository;
import com.playdata.homelesscode.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ServerRepository serverRepository;
    private final ServerJoinUserListRepository serverListRepository;
    private final ChatServiceClient chatServiceClient;



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
            Role role = server.getRole(); // role 가져오기
            if ("OWNER".equals(role) || "ROLE".equals(role)) {
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

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();

        Channel channel = channelRepository.findById(id).orElseThrow();

        ServerJoinUserList byEmailAndServerId = serverListRepository.findByEmailAndServerId(userEmail, channel.getServer().getId());

        log.info("롤은 ? {}", byEmailAndServerId.getRole());

        if(byEmailAndServerId.getRole() == Role.OWNER || byEmailAndServerId.getRole() == Role.MANAGER){
            channelRepository.deleteById(id);
            chatServiceClient.deleteChatMessageByChannelId(id);
        }else {
            throw new CustomThrowException("권한이 없습니다.");
        }


    }

    public Channel updateChannel(ChannelUpdateDto dto) {

        Channel channel = channelRepository.findById(dto.getChannelId()).orElseThrow();

        channel.setName(dto.getName());


        Channel save = channelRepository.save(channel);

        return save;

    }

}
