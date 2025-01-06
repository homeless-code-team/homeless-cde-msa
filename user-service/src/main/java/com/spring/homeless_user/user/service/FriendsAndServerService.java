package com.spring.homeless_user.user.service;

import com.spring.homeless_user.common.utill.SecurityContextUtil;
import com.spring.homeless_user.user.dto.CommonResDto;
import com.spring.homeless_user.user.dto.ServerDto;
import com.spring.homeless_user.user.dto.ServerResDto;
import com.spring.homeless_user.user.dto.friendsDto;
import com.spring.homeless_user.user.entity.Friends;
import com.spring.homeless_user.user.entity.Servers;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.FriendsRepository;
import com.spring.homeless_user.user.repository.ServerRepository;
import com.spring.homeless_user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@Slf4j
public class FriendsAndServerService {

    private final ServerRepository serverRepository;
    private final FriendsRepository friendsRepository;
    private final UserRepository userRepository;
    private final SecurityContextUtil securityContextUtil;
    private final RedisTemplate<String, String> friendsTemplate;
    private final RedisTemplate<String, String> serverTemplate;

    public FriendsAndServerService(ServerRepository serverRepository,
                                   FriendsRepository friendsRepository,
                                   UserRepository userRepository,
                                   SecurityContextUtil securityContextUtil,
                                   @Qualifier("friends") RedisTemplate<String, String> friendsTemplate,
                                   @Qualifier("server") RedisTemplate<String, String> serverTemplate) {
        this.serverRepository = serverRepository;
        this.friendsRepository = friendsRepository;
        this.userRepository = userRepository;
        this.securityContextUtil = securityContextUtil;
        this.friendsTemplate = friendsTemplate;
        this.serverTemplate = serverTemplate;
    }

    // 친구요청
    public CommonResDto addFriends(friendsDto dto) {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends","/api/v1/users","Delete"));

        try {
            //사용자 가져오기
            String email = securityContextUtil.getCurrentUser().getEmail();

            // Redis에 친구 요청 확인
            String reqKey = email;// 요청 키
            String resKey = dto.getResEmail(); // 응답 키

            // 친구대상이 있는지 확인
            User resUser = userRepository.findByEmail(resKey)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + resKey));

            // 이미 요청이 진행 중인지 확인
            if (Boolean.TRUE.equals(friendsTemplate.opsForSet().isMember(email, resKey))) {
                return new CommonResDto(HttpStatus.BAD_REQUEST,400, "이미 친구요청이 진행중입니다", null,links);
            }

            // Redis에 요청 저장 (요청자와 응답자 양쪽에 저장)
            friendsTemplate.opsForSet().add(reqKey, resKey);
            friendsTemplate.opsForSet().add(resKey, reqKey);

            return new CommonResDto(HttpStatus.OK,200, "친구요청 완료", null,links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,401, "에러발생: " + e.getMessage(), null,links);
        }
    }

    // 친구목록 조회
    public CommonResDto UserFriends() {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends","/api/v1/users","Delete"));
        try{
            String email = securityContextUtil.getCurrentUser().getEmail();
            List<Friends> friends = friendsRepository.findByUserEmail(email);

            // 친구 목록이 없을 경우 처리
            if (friends == null || friends.isEmpty()) {
                return new CommonResDto(HttpStatus.OK,200, "친구 목록이 비어 있습니다.", Collections.emptyList(),links);
            }
            // 닉네임 목록으로 변환
            List<String> friendemail = friends.stream()
                    .map(friend -> friend.getFriend().getEmail()) // 친구의 닉네임만 추출
                    .collect(Collectors.toList());

            return new CommonResDto(HttpStatus.OK,200, "친구목록 조회 성공", friendemail,links);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,400,"에러발생"+e.getMessage(),null,links);
        }
    }

    // 친구 삭제
    public CommonResDto deleteFriend(friendsDto dto) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends","/api/v1/users","Delete"));
        try {
            // 현재 사용자 정보 가져오기
            String email = securityContextUtil.getCurrentUser().getEmail();

            // 요청자(User) 조회
            User reqUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

            // 응답자(User) 조회
            User resUser = userRepository.findByEmail(dto.getResEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " +dto.getResEmail()));

            // 요청자 -> 응답자 관계 삭제
            Friends friendToRemove = friendsRepository.findByUserEmailAndFriendEmail(email, resUser.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Friend relationship not found"));

            // 응답자 -> 요청자 관계 삭제
            Friends reverseFriendToRemove = friendsRepository.findByUserEmailAndFriendEmail(resUser.getEmail(),email)
                    .orElseThrow(() -> new IllegalArgumentException("Reverse friend relationship not found"));

            // 관계 삭제
            friendsRepository.delete(friendToRemove);
            friendsRepository.delete(reverseFriendToRemove);

            return new CommonResDto(HttpStatus.OK,200, "양방향 친구 관계가 삭제되었습니다.", null,links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,401, "에러 발생: " + e.getMessage(), null,links);
        }
    }

    // 요청응답
    @Transactional
    public CommonResDto addResFriends(friendsDto dto) {
        List<CommonResDto.Link> links = List.of(
                new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"),
                new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"),
                new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE")
        );

        try {
            String reqEmail = securityContextUtil.getCurrentUser().getEmail();
            String resEmail = dto.getResEmail();

            if (resEmail == null || resEmail.isEmpty()) {
                throw new IllegalArgumentException("Response email cannot be null or empty");
            }

            // 사용자 객체 로드
            User requester = userRepository.findByEmail(reqEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Requester not found: " + reqEmail));
            User responder = userRepository.findByEmail(resEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Responder not found: " + resEmail));

            // 친구 관계 생성
            Friends friend1 = new Friends(requester, responder);
            Friends friend2 = new Friends(responder, requester);

            friendsRepository.save(friend1);
            friendsRepository.save(friend2);

            // Redis에서 요청 제거
            friendsTemplate.opsForSet().remove(reqEmail, resEmail);
            friendsTemplate.opsForSet().remove(resEmail, reqEmail);

            log.info("Friends successfully added between {} and {}", reqEmail, resEmail);
            return new CommonResDto(HttpStatus.OK, 200, "친구가 되었습니다.", null, links);

        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "잘못된 입력: " + e.getMessage(), null, links);

        } catch (Exception e) {
            log.error("Unexpected error during friend addition: {}", e.getMessage(), e);
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 에러 발생: " + e.getMessage(), null, links);
        }
    }

    // 요청 목록 조회
    public CommonResDto addFriendsJoin() {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE"));

        // 현재 사용자 이메일 가져오기
        String email = securityContextUtil.getCurrentUser().getEmail();
        String reqKey = email; // 요청 리스트 키

        // Redis에서 Set 반환
        Set<String> memberSet = friendsTemplate.opsForSet().members(reqKey);

        // Set을 List로 변환
        List<String> members = new ArrayList<>(memberSet);

        // 응답 생성
        return new CommonResDto(HttpStatus.OK, 200, "친구 요청 조회를 완료했습니다.", members, links);
    }

////////////////////////////////// 서버 관리 ///////////////////////////////////////////////////////////////////////////////

    // 서버 가입 신청
    public CommonResDto addReqServer(ServerDto dto) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "Delete"));

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

            // Redis에 서버 ID 추가
            if (dto.getServerId() == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 서버 ID입니다.", null, links);
            }

            // Redis에 기존 값 확인
            Set<String> existingServerIds = serverTemplate.opsForSet().members(email);
            if (existingServerIds != null && existingServerIds.contains(String.valueOf(dto.getServerId()))) {
                return new CommonResDto(HttpStatus.OK, 200, "이미 가입이 진행 중입니다.", null, links);
            }

            // Redis에 서버 ID 추가
            serverTemplate.opsForSet().add(email, String.valueOf(dto.getServerId()));
            return new CommonResDto(HttpStatus.OK, 200, "서버 가입 요청이 완료되었습니다.", null, links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 가입 요청 중 오류가 발생했습니다.", e.getMessage(), links);
        }
    }
    // 서버 가입 응답
    public CommonResDto addResServer(ServerDto dto) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE"));

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            log.info("Requester Email: {}", email);

            // 사용자 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

            // AddStatus 검증
            if (dto.getAddStatus() == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "Status를 결정해주세요 (APPROVE 또는 REJECT)", null, links);
            }

            // Redis에서 serverId 가져오기
            String serverIdStr = serverTemplate.opsForSet().pop(email);
            if (serverIdStr == null || serverIdStr.isEmpty()) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "해당 요청에 대한 serverId가 없습니다.", null, links);
            }

            // 안전하게 Long 변환
            int serverId;
            try {
                serverId = Integer.parseInt(serverIdStr);
            } catch (NumberFormatException e) {
                log.error("Invalid serverId format in Redis: {}", serverIdStr, e);
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "Redis에 저장된 serverId 형식이 올바르지 않습니다.", null, links);
            }

            if ("APPROVE".equalsIgnoreCase(String.valueOf(dto.getAddStatus()))) {
                // 서버 생성 및 저장
                Servers server = new Servers();
                server.setServerId(serverId);
                server.setUser(user);

                user.getServerList().add(server);
                serverRepository.save(server);

                return new CommonResDto(HttpStatus.OK, 200, "서버 가입이 승인되었습니다.", null, links);
            } else if ("REJECT".equalsIgnoreCase(String.valueOf(dto.getAddStatus()))) {
                // 서버 가입 요청 거절
                serverTemplate.opsForSet().remove(email, String.valueOf(serverId));

                return new CommonResDto(HttpStatus.OK, 200, "서버 가입이 거절되었습니다.", null, links);
            }

            return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "Invalid AddStatus", null, links);
        } catch (Exception e) {
            log.error("Error in addResServer: {}", e.getMessage(), e);
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 응답 처리 중 오류 발생", e.getMessage(), links);
        }
    }


    // 서버 가입 요청 조회
    public CommonResDto addServerJoin() {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addServers", "/api/v1/users/servers", "POST"));
        links.add(new CommonResDto.Link("ListServers", "/api/v1/users/servers", "GET"));
        links.add(new CommonResDto.Link("DeleteServers", "/api/v1/users/servers", "DELETE"));

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            // Redis에서 모든 요청 서버 ID 조회
            Set<String> allData = serverTemplate.opsForSet().members(email);

            if (allData == null || allData.isEmpty()) {
                return new CommonResDto(HttpStatus.OK, 200, "가입 요청된 서버가 없습니다.", null, links);
            }

            // 요청 데이터 처리
            List<ServerResDto> serverResList = allData.stream()
                    .map(serverId -> new ServerResDto(serverId, null))
                    .collect(Collectors.toList());

            return new CommonResDto(HttpStatus.OK, 200, "가입 요청 서버 목록 조회 성공", serverResList, links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 요청 조회 중 오류 발생", null, links);
        }
    }


    // Redis 데이터 삭제 로직
    private void deleteRedisServerData(String reqEmail, Long serverId) {
        try {
            serverTemplate.opsForSet().remove(reqEmail, String.valueOf(serverId));
        } catch (Exception e) {
            // Redis 삭제 실패 시 로그 추가
            System.err.println("Redis 데이터 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    private boolean checkToken(String reqToken, String redisToken) {
        return reqToken.equals(redisToken);
    }

    ///feign 통신

//    public void findMember(String id) {
//
//        userRepository.fin
}
