package com.playdata.homelesscode.service;

//import com.playdata.homelesscode.common.config.AwsS3Config;

import com.playdata.homelesscode.client.ChatServiceClient;
import com.playdata.homelesscode.client.UserServiceClient;
import com.playdata.homelesscode.common.config.AwsS3Config;
import com.playdata.homelesscode.common.custom.CustomThrowException;
import com.playdata.homelesscode.common.dto.CommonResDto;
import com.playdata.homelesscode.common.utill.SecurityContextUtil;
import com.playdata.homelesscode.dto.boardList.BoardListCreateDto;
import com.playdata.homelesscode.dto.boardList.BoardListUpdateDto;
import com.playdata.homelesscode.dto.boards.BoardCreateDto;
import com.playdata.homelesscode.dto.boards.BoardDeleteDto;
import com.playdata.homelesscode.dto.boards.BoardSearchDto;
import com.playdata.homelesscode.dto.boards.BoardUpdateDto;
import com.playdata.homelesscode.dto.channel.ChannelCreateDto;
import com.playdata.homelesscode.dto.channel.ChannelResponseDto;
import com.playdata.homelesscode.dto.channel.ChannelUpdateDto;
import com.playdata.homelesscode.dto.server.*;
import com.playdata.homelesscode.dto.user.UserReponseInRoleDto;
import com.playdata.homelesscode.dto.user.UserResponseDto;
import com.playdata.homelesscode.entity.*;
import com.playdata.homelesscode.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public ServerService(ServerRepository serverRepository,
                         ServerJoinUserListRepository serverListRepository,
                         UserServiceClient userServiceClient,
                         AwsS3Config s3Config,
                         @Qualifier("server")
                         RedisTemplate<String, String> serverTemplate
                         ) {
        this.serverRepository = serverRepository;
        this.userServiceClient = userServiceClient;
        this.serverListRepository = serverListRepository;

        this.serverTemplate = serverTemplate;
        this.s3Config = s3Config;

    }


    public Server createServer(ServerCreateDto dto) throws IOException {

        String userEmail = SecurityContextUtil.getCurrentUser().getEmail();

        log.info("이메일1111, {}", userEmail);
        Server server = dto.toEntity();
        server.setEmail(userEmail);
        server.setServerType(1);



        if(dto.getServerImg() != null){
            String fileName = UUID.randomUUID() + "-"  + dto.getServerImg().getOriginalFilename();

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
    public CommonResDto addReqServer(ServerDto dto) {


        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            // Redis에 서버 ID 추가
            if (dto.getServerId() == null) {
                return new CommonResDto(HttpStatus.OK, "유효하지 않은 서버 ID 입니다ㅏ", null);
            }

            // Redis에 기존 값 확인
            Set<String> existingServerIds = serverTemplate.opsForSet().members(email);
            log.info(existingServerIds.toString());

            if (existingServerIds != null && existingServerIds.contains(dto.getServerId())) {
                return new CommonResDto(HttpStatus.OK, "이미 가입이 진행 중입니다.", null);
            }

            // Redis에 서버 ID 추가
            serverTemplate.opsForSet().add(email, dto.getServerId());

            return new CommonResDto(HttpStatus.OK, "서버 가입 요청이 완료되었습니다.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 가입 요청 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    // 서버 가입 응답
    public CommonResDto addResServer(ServerDto dto) {

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            log.info("Requester Email: {}", email);


            // 사용자 조회
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

            // AddStatus 검증
            if (dto.getAddStatus() == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST,  "status를 결정해주세요 (APPROVE 또는 REJECT)", null);
            }

            // Redis에서 serverId 가져오기
            String serverId = serverTemplate.opsForSet().pop(email);
            log.info(serverId.toString());


            if (serverId == null || serverId.isEmpty()) {
                return new CommonResDto(HttpStatus.BAD_REQUEST,  "해당 요청에 대한 serverId가 없습니다.", null);
            }


            if (dto.getAddStatus() == AddStatus.ACCEPT) {

                Server server = serverRepository.findById(serverId).orElseThrow();

                ServerJoinUserList serverJoinUserList = ServerJoinUserList.builder()
                        .server(server)
                        .email(email)
                        .role(Role.GENERAL)
                        .build();

                serverListRepository.save(serverJoinUserList);

                return new CommonResDto(HttpStatus.OK, "서버 가입이 승인되었습니다.", null);

            } else if (dto.getAddStatus() == AddStatus.REJECTED) {

                // 서버 가입 요청 거절
                deleteRedisServerData(email, serverId.toString());

                return new CommonResDto(HttpStatus.OK,  "서버 가입을 거절하셨습니다.", null);
            }

            return new CommonResDto(HttpStatus.BAD_REQUEST,  "Invalid AddStatus", null);
        } catch (Exception e) {
            log.error("Error in addResServer: {}", e.getMessage(), e);
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,  "서버 응답 처리 중 오류 발생", e.getMessage());
        }
    }


    // 서버 가입 요청 조회
    public CommonResDto addServerJoin(String serverId) {


        try {

            // Redis에서 모든 요청 서버 ID 조회
            List<String> keysByValue = findKeysByValue(serverId);


            return new CommonResDto(HttpStatus.OK,  "가입 요청 서버 목록 조회 성공", keysByValue);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,  "서버 요청 조회 중 오류 발생", null);
        }
    }


    // Redis 데이터 삭제 로직
    private void deleteRedisServerData(String email, String serverId) {
        try {
            serverTemplate.opsForSet().remove(email, serverId);
            serverTemplate.opsForSet().remove(serverId, email);
        } catch (Exception e) {
            // Redis 삭제 실패 시 로그 추가
            System.err.println("Redis 데이터 삭제 중 오류 발생: " + e.getMessage());
        }
    }




    // Redis에서 value로 조회하기 위한 전체 조회
    public List<String> findKeysByValue(String targetValue) {
        List<String> matchingKeys = new ArrayList<>(); // 결과를 저장할 리스트
        Set<String> keys = serverTemplate.keys("*"); // 모든 키 가져오기

        if (keys != null) {
            for (String key : keys) {
                Set<String> values = serverTemplate.opsForSet().members(key); // 각 키의 값 가져오기
                if (values != null && values.contains(targetValue)) {
                    matchingKeys.add(key); // 일치하는 키를 리스트에 추가
                }
            }
        }

        return matchingKeys; // 매칭된 키 리스트 반환
    }

//    public Page<UserReponseInRoleDto> getUserList(String id, Pageable pageable) {
//
//        List<ServerJoinUserList> byServerId = serverListRepository.findByServerId(id);
//
//        List<String> userEmails = byServerId.stream().map(s -> s.getEmail()).collect(Collectors.toList());
//
//        int start = (int) pageable.getOffset();
//        int end = Math.min((start + pageable.getPageSize()), userEmails.size());
//
//
//        List<UserResponseDto> byEmailIn = userServiceClient.findByEmailIn(userEmails, pageable);
//
//
//
//        List<UserReponseInRoleDto> userList = byEmailIn.stream().map(dto -> {
//            ServerJoinUserList serverJoinUserList
//                    = byServerId.stream().filter(user -> user
//                            .getEmail().equals(dto.getEmail()))
//                    .findFirst().orElseThrow(null);
//
//            return new UserReponseInRoleDto(dto.getId(), dto.getNickname(), dto.getEmail(), dto.getProfileImage(), serverJoinUserList.getRole());
//        }).collect(Collectors.toList());
//
//
//        List<UserReponseInRoleDto> pagedUserList = userList.subList(start, end);
//        return new PageImpl<>(pagedUserList, pageable, userList.size());
//    }


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
        log.info(dto.getEmail());
        log.info(String.valueOf(dto.getRole()));


        ServerJoinUserList byEmail = serverListRepository.findByEmailAndServerId(dto.getEmail(), dto.getId());






        log.info("헤헿 {}",byEmail.getEmail());
        byEmail.setRole(dto.getRole());


        ServerJoinUserList save = serverListRepository.save(byEmail);

        log.info("롤은 , {}", save.getRole());
    }

    public void resignUser(ResignUserDto dto) {

        serverListRepository.deleteByServerIdAndEmail(dto.getServerId(), dto.getEmail());

    }
}
