package com.playdata.homelesscode.service;

//import com.playdata.homelesscode.common.config.AwsS3Config;

import com.playdata.homelesscode.client.UserServiceClient;
import com.playdata.homelesscode.common.config.AwsS3Config;
import com.playdata.homelesscode.common.dto.CommonResDto;
import com.playdata.homelesscode.common.utill.SecurityContextUtil;
import com.playdata.homelesscode.dto.server.*;
import com.playdata.homelesscode.dto.user.UserReponseInRoleDto;
import com.playdata.homelesscode.dto.user.UserResponseDto;
import com.playdata.homelesscode.entity.AddStatus;
import com.playdata.homelesscode.entity.Server;
import com.playdata.homelesscode.entity.ServerJoinUserList;
import com.playdata.homelesscode.repository.ServerJoinUserListRepository;
import com.playdata.homelesscode.repository.ServerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ServerService {

    private final ServerRepository serverRepository;
    private final ServerJoinUserListRepository serverListRepository;
    private final AwsS3Config s3Config;
    private final UserServiceClient userServiceClient;


    private final RedisTemplate<String, String> serverTemplate;
    private final RedisTemplate server;

    public ServerService(ServerRepository serverRepository,
                         ServerJoinUserListRepository serverListRepository,
                         UserServiceClient userServiceClient,
                         AwsS3Config s3Config,
                         @Qualifier("server")
                         RedisTemplate<String, String> serverTemplate,
                         @Qualifier("server") RedisTemplate server) {
        this.serverRepository = serverRepository;
        this.userServiceClient = userServiceClient;
        this.serverListRepository = serverListRepository;
        this.serverTemplate = serverTemplate;
        this.s3Config = s3Config;
        this.server = server;
    }


    public Server createServer(ServerCreateDto dto) throws IOException {

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();

        log.info("이메일1111, {}", userEmail);
        Server server = dto.toEntity();
        server.setEmail(userEmail);
        server.setServerType(1);


        if (dto.getServerImg() != null) {
            String fileName = UUID.randomUUID() + "-" + dto.getServerImg().getOriginalFilename();

            String imageUrl = s3Config.uploadToS3Bucket(dto.getServerImg().getBytes(), fileName);


            server.setServerImg(imageUrl);
        }


        Server result = serverRepository.save(server);


        log.info("이메일, {}", userEmail);

        ServerJoinUserList serverList = ServerJoinUserList.builder()
                .server(server)
                .email(userEmail)
                .role(Role.OWNER)
                .build();


        ServerJoinUserList save = serverListRepository.save(serverList);


        log.info("여기는 서비스 롤은  {}", save.getRole());

        return result;
    }

    public List<ServerResponseDto> getServer() {

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();

        List<ServerJoinUserList> byUserId = serverListRepository.findByEmail(userEmail);

        List<String> collect = byUserId.stream().map(s -> s.getServer().getId()).collect(Collectors.toList());

        List<Server> byIdIn = serverRepository.findByIdInOrServerTypeOrderByServerTypeAsc(collect, 0);


        List<ServerResponseDto> collect1 = byIdIn.stream().map(server -> {
            ServerJoinUserList serverJoinUserList = byUserId.stream().filter(s -> s.getServer().getId().equals(server.getId()))
                    .findFirst().orElse(null);

            Role role = (serverJoinUserList != null) ? serverJoinUserList.getRole() : Role.GENERAL;

            return new ServerResponseDto(server.getId(),
                    server.getTag(),
                    server.getTitle(),
                    server.getServerImg(),
                    server.getEmail(),
                    server.getServerType(),
                    role);

        }).collect(Collectors.toList());


        return collect1;

    }

    public void deleteServer(String id) {

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();

        ServerJoinUserList serverList = serverListRepository.findByEmailAndServerId(userEmail, id);


        Server server = serverRepository.findById(id).orElseThrow();

        if (server.getServerImg() != null) {
            try {
                s3Config.deleteFromS3Bucket(server.getServerImg());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        serverRepository.deleteById(id);


    }


    public void deleteServerList(String id) {

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();

        log.info("아이디 {}", id);
        log.info("이메일 {}", userEmail);


        serverListRepository.deleteByServerIdAndEmail(id, userEmail);


    }


    ////////////////////////////////// 서버 관리 ///////////////////////////////////////////////////////////////////////////////

    // 서버 가입 신청
    public CommonResDto<Void> addReqServer(ServerDto dto) {


        log.info("서버 아이디 {}", dto.getServerId() );
        log.info("이메일 {}", dto.getEmail() );
        
        
        try {
//            String email = SecurityContextUtil.getCurrentUser().getEmail();

            // Redis에 서버 ID 추가
            if (dto.getServerId() == null) {
                return new CommonResDto<Void>(HttpStatus.OK, "유효하지 않은 서버 ID 입니다ㅏ", null);
            }

            // Redis에 기존 값 확인
            Set<String> existingServerIds = serverTemplate.opsForSet().members(dto.getEmail());
            log.info(existingServerIds.toString());

            if (existingServerIds.contains(dto.getServerId())) {
                return new CommonResDto<Void>(HttpStatus.OK, "이미 초대한 회원입니다..", null);
            }

            // Redis에 서버 ID 추가
            serverTemplate.opsForSet().add(dto.getEmail(), dto.getServerId());
            serverTemplate.opsForSet().add(dto.getServerId(), dto.getEmail());



            return new CommonResDto<Void>(HttpStatus.OK, "서버 가입 요청이 완료되었습니다.", null);

        } catch (Exception e) {
            return new CommonResDto<Void>(HttpStatus.INTERNAL_SERVER_ERROR, "서버 초대 중 오류가 발생했습니다..",null);
        }
    }

    // 서버 가입 응답
    public CommonResDto addResServer(ServerDto dto) {

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            if (dto.getAddStatus() == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "status를 결정해주세요 (APPROVE 또는 REJECT)", null);
            }



            if (dto.getServerId() == null || dto.getServerId().isEmpty()) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "해당 요청에 대한 serverId가 없습니다.", null);
            }


            if (dto.getAddStatus() == AddStatus.ACCEPT) {

                Server server = serverRepository.findById(dto.getServerId()).orElseThrow();

                ServerJoinUserList serverJoinUserList = ServerJoinUserList.builder()
                        .server(server)
                        .email(email)
                        .role(Role.GENERAL)
                        .build();

                serverListRepository.save(serverJoinUserList);


                deleteRedisServerData(email, dto.getServerId());

                return new CommonResDto(HttpStatus.OK, "서버 가입이 승인되었습니다.", null);

            } else if (dto.getAddStatus() == AddStatus.REJECTED) {


                deleteRedisServerData(email, dto.getServerId());

                return new CommonResDto(HttpStatus.OK, "서버 가입을 거절하셨습니다.", null);
            }

            return new CommonResDto(HttpStatus.BAD_REQUEST, "Invalid AddStatus", null);
        } catch (Exception e) {
            log.error("Error in addResServer: {}", e.getMessage(), e);
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 응답 처리 중 오류 발생", e.getMessage());
        }
    }





    // Redis 데이터 삭제 로직
    private void deleteRedisServerData(String email, String serverId) {
        try {
            serverTemplate.opsForSet().remove(email, serverId);
            serverTemplate.opsForSet().remove(serverId, email);
        } catch (Exception e) {
            System.err.println("Redis 데이터 삭제 중 오류 발생: " + e.getMessage());
        }
    }



    public List<String> findValuesByKey(String Key) {
        List<String> serverList = new ArrayList<>();

        Set<String> servers = serverTemplate.opsForSet().members(Key);

        if (servers != null) {
            serverList.addAll(servers);
        }

        log.info("이메일 '{}'에 해당하는 서버 리스트: {}", Key, serverList);
        return serverList;
    }


    // 서버 가입 보낸거 조회
    public CommonResDto addServerJoin(String serverId) {
        try {

            List<String> valuesByKeys = findValuesByKey(serverId);


            List<UserResponseDto> users = userServiceClient.findByEmailIn(valuesByKeys);


            return new CommonResDto(HttpStatus.OK, "가입 요청 서버 목록 조회 성공", users);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 요청 조회 중 오류 발생", null);
        }
    }


    // 서버 초대 요청 리스트
    public CommonResDto<Void> getInviteList() {

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            // Redis에서 모든 요청 서버 ID 조회
            List<String> keysByValue = findValuesByKey(email);


            List<Server> servers = serverRepository.findByIdIn(keysByValue);

            List<inviteServerList> serverList = servers.stream().map(server -> new inviteServerList(server)).toList();


            return new CommonResDto(HttpStatus.OK, "가입 요청 서버 목록 조회 성공",serverList );

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 요청 조회 중 오류 발생", null);
        }


    }



    public List<UserReponseInRoleDto> getUserList(String id) {

        List<ServerJoinUserList> byServerId = serverListRepository.findByServerId(id);
        List<String> userEmails = byServerId.stream().map(ServerJoinUserList::getEmail).collect(Collectors.toList());

        List<UserResponseDto> byEmailIn = userServiceClient.findByEmailIn(userEmails);

        List<UserReponseInRoleDto> userList = byEmailIn.stream().map(dto -> {
            ServerJoinUserList serverJoinUserList = byServerId.stream()
                    .filter(user -> user.getEmail().equals(dto.getEmail()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + dto.getEmail()));

            return new UserReponseInRoleDto(
                    dto.getId(),
                    dto.getNickname(),
                    dto.getEmail(),
                    dto.getProfileImage(),
                    serverJoinUserList.getRole()
            );
        }).collect(Collectors.toList());

        return userList;

    }

    public void changeRole(ChangeRoleDto dto) {

        ServerJoinUserList byEmail = serverListRepository.findByEmailAndServerId(dto.getEmail(), dto.getId());
        byEmail.setRole(dto.getRole());

        ServerJoinUserList save = serverListRepository.save(byEmail);


    }

    public void resignUser(ResignUserDto dto) {

        serverListRepository.deleteByServerIdAndEmail(dto.getServerId(), dto.getEmail());

    }

    public void editServer(ServerEditDto dto) throws IOException {


        Server server = serverRepository.findById(dto.getId()).orElseThrow();

        if (dto.getTitle() != null) {
            server.setTitle(dto.getTitle());
        }

        if (dto.getTag() != null) {
            server.setTag(dto.getTag());
        }


        if (dto.getServerImg() != null) {

            if (server.getServerImg() != null) {
                try {
                    s3Config.deleteFromS3Bucket(server.getServerImg());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }

            String fileName = UUID.randomUUID() + "-" + dto.getServerImg().getOriginalFilename();

            String imageUrl = s3Config.uploadToS3Bucket(dto.getServerImg().getBytes(), fileName);

            server.setServerImg(imageUrl);
        }

        serverRepository.save(server);

    }

    public List<ServerResponseDto> getServerList() {

        String email = SecurityContextUtil.getCurrentUser().getEmail();

        List<String> role = new ArrayList<>();

        role.add("OWNER");
        role.add("MANAGER");

        List<ServerJoinUserList> byEmailAndRole = serverListRepository.findByEmailAndRoleIn(email, role);
        for (ServerJoinUserList serverJoinUserList : byEmailAndRole) {
            serverJoinUserList.getServer().getId();
        }


        List<String> collect = byEmailAndRole.stream().map(s -> s.getServer().getId()).collect(Collectors.toList());


        List<Server> byIdIn = serverRepository.findByIdIn(collect);

        List<ServerResponseDto> result = byIdIn.stream().map(server -> {
            return new ServerResponseDto(
                    server.getId(),
                    server.getTag(),
                    server.getTitle(),
                    server.getServerImg(),
                    null, 0, null
            );
        }).collect(Collectors.toList());


        return result;

    }

    public CommonResDto<Void> cancelInvite(String serverId, String email) {

        try {

            deleteRedisServerData(email,serverId);


            return new CommonResDto(HttpStatus.OK, "서버 초대 요청 취소 성공", null );

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 초대 요청 취소 중 오류 발생", null);
        }


    }
}
