package com.spring.homelesscode.friends_server.service;


import com.spring.homelesscode.friends_server.cofig.UserServiceClient;
import com.spring.homelesscode.friends_server.common.utill.SecurityContextUtil;
import com.spring.homelesscode.friends_server.dto.CommonResDto;
import com.spring.homelesscode.friends_server.dto.FriendsDto;
import com.spring.homelesscode.friends_server.entity.AddStatus;
import com.spring.homelesscode.friends_server.entity.Friends;
import com.spring.homelesscode.friends_server.repository.FriendsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Slf4j
public class FriendsService {

    private final FriendsRepository friendsRepository;
    private final SecurityContextUtil securityContextUtil;
    private final RedisTemplate<String, String> friendsTemplate;
    private final RedisTemplate<String, String> serverTemplate;
    private final UserServiceClient userServiceclient;

    public FriendsService(FriendsRepository friendsRepository,
                          SecurityContextUtil securityContextUtil,
                          @Qualifier("friends") RedisTemplate<String, String> friendsTemplate,
                          @Qualifier("server") RedisTemplate<String, String> serverTemplate, UserServiceClient userServiceclient) {
        this.friendsRepository = friendsRepository;
        this.securityContextUtil = securityContextUtil;
        this.friendsTemplate = friendsTemplate;
        this.serverTemplate = serverTemplate;
        this.userServiceclient = userServiceclient;
    }

    // 친구요청
    public CommonResDto addFriends(FriendsDto dto) {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/friends", "Delete"));

        try {
            //사용자 가져오기
            String nickname = securityContextUtil.getCurrentUser().getNickname();

            // 친구대상이 있는지 확인
            List<Friends> resEmail = friendsRepository.findByNickname(nickname);
            boolean flag = resEmail.contains(nickname);
            log.info(String.valueOf(flag));
            log.info(String.valueOf(Collections.unmodifiableList(resEmail)));
            List<String> keysByValue = findKeysByValue(nickname);
            log.info(String.valueOf(Collections.unmodifiableList(keysByValue)));
            //친구관계인지 확인
            if (flag) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "이미 친구관계입니다", null, links);
            }

            // 이미 요청이 진행 중인지 확인
            if (keysByValue == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "이미 친구요청이 진행중입니다", null, links);
            }

            // Redis에 요청 저장
            friendsTemplate.opsForSet().add(dto.getResNickname(), nickname);

            return new CommonResDto(HttpStatus.OK, 200, "친구요청 완료", null, links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 401, "에러발생: " + e.getMessage(), null, links);
        }
    }

    // 친구목록 조회
    public CommonResDto UserFriends() {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/friends", "Delete"));
        try {
            String nickname = SecurityContextUtil.getCurrentUser().getNickname();

            // JPQL로 친구 목록 조회 (refreshToken 여부 포함)
            List<Friends> results = friendsRepository.findByNickname(nickname);


            // 친구 목록이 없을 경우 처리
            if (results == null || results.isEmpty()) {
                return new CommonResDto(HttpStatus.OK, 200, "친구 목록이 비어 있습니다.", Collections.emptyList(), links);
            }
            // refreshToken 확인 및 결과 생성
            // refreshToken 확인 및 결과 생성
            List<Map<String, Object>> response = new ArrayList<>();
            for (Friends friend : results) {
                String friendNickname = friend.getNickname();

                // Feign 클라이언트를 통해 UserService와 통신
                boolean hasRefreshToken = userServiceclient.existsByNicknameAndRefreshToken(friendNickname);

                Map<String, Object> friendData = new HashMap<>();
                friendData.put("nickname", friendNickname);
                friendData.put("refreshToken", hasRefreshToken ? 1 : 0);

                response.add(friendData);
            }
            //친구목록을 사용자에게 반환
            return new CommonResDto(HttpStatus.OK, 200, "친구목록 조회 성공", response, links);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "에러발생" + e.getMessage(), null, links);
        }
    }

    // 친구 삭제
    public CommonResDto deleteFriend(FriendsDto dto) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/friends", "Delete"));
        try {
            // 현재 사용자 정보 가져오기
            String nickname = securityContextUtil.getCurrentUser().getNickname();
            String resNickname = dto.getResNickname();

            // 요청자 -> 응답자 관계 확인
            List<Friends> userFriends = friendsRepository.findByNickname(nickname);

            // 응답자 -> 요청자 관계 확인
            List<Friends> resFreiends = friendsRepository.findByNickname(resNickname);

            // 저장 삭제
            userFriends.removeIf(friend -> friend.getNickname().equals(resNickname));
            resFreiends.removeIf(friend -> friend.getNickname().equals(nickname));

            // 저장 작업이 필요한 경우
            friendsRepository.saveAll(userFriends);
            friendsRepository.saveAll(resFreiends);

            return new CommonResDto(HttpStatus.OK, 200, "친구 관계가 삭제되었습니다.", null, links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 401, "에러 발생: " + e.getMessage(), null, links);
        }
    }

    // 요청응답
    @Transactional
    public CommonResDto addResFriends(FriendsDto dto) {
        List<CommonResDto.Link> links = List.of(
                new CommonResDto.Link("addFriends", "/api/v1/friends", "POST"),
                new CommonResDto.Link("ListFriends", "/api/v1/friends", "GET"),
                new CommonResDto.Link("DeleteFriends", "/api/v1/friends", "DELETE")
        );

        try {

            String nickname = SecurityContextUtil.getCurrentUser().getNickname();
            String resNickname = dto.getResNickname();

            if (resNickname == null || resNickname.isEmpty()) {
                throw new IllegalArgumentException("Response email cannot be null or empty");
            }

            // 요청자 -> 응답자 관계 확인
            List<Friends> userFriends = friendsRepository.findByNickname(nickname);

            // 응답자 -> 요청자 관계 확인
            List<Friends> resFreiends = friendsRepository.findByNickname(resNickname);

            // 사용자 객체 로드
            Boolean isMember = friendsTemplate.opsForSet().isMember(resNickname, nickname);
            log.info(String.valueOf(isMember));

            if (dto.getAddStatus() == AddStatus.ACCEPT && Boolean.TRUE.equals(isMember)) {

                // 관계 확인
                if (userFriends.contains(resNickname) && resFreiends.contains(nickname) ) {
                    log.info("Friend relationship already exists");
                    return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "이미 친구입니다.", null, links);
                }

                //친구관계 객체 생성
                Friends make1 = new Friends();
                make1.setNickname(nickname);
                make1.setNicknames(resNickname);

                Friends make2 = new Friends();
                make2.setNickname(resNickname);
                make2.setNicknames(nickname);

                //저장
                friendsRepository.save(make1);
                friendsRepository.save(make2);
                
                // Redis에서 요청 제거
                friendsTemplate.opsForSet().remove(resNickname, nickname);

                log.info("Friends successfully added between {} and {}", nickname, resNickname);
                return new CommonResDto(HttpStatus.OK, 200, "친구가 되었습니다.", null, links);
            }
            if (dto.getAddStatus() == AddStatus.REJECTED && Boolean.TRUE.equals(isMember)) {
                friendsTemplate.opsForSet().remove(resNickname, nickname);
                return new CommonResDto(HttpStatus.OK, 200, "친구추가를 거절하셨습니다.", null, links);
            }
            return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "ACCEPT 와 REJECT 중 하나를 선택해 주세요", null, links);

        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "잘못된 입력: " + e.getMessage(), null, links);

        } catch (Exception e) {
            log.error("Unexpected error during friend addition: {}", e.getMessage(), e);
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 에러 발생: " + e.getMessage(), null, links);
        }
    }

    // 요청받은  친구 목록 조회
    public CommonResDto resFriendsJoin() {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE"));

        try {      // 현재 사용자 이메일 가져오기
            String nickname = SecurityContextUtil.getCurrentUser().getNickname();

            //value 값으로 조회
            Set<String> value = friendsTemplate.opsForSet().members(nickname);
            List<String> valueList = new ArrayList<>(value); // Set을 List로 변환
            log.info(valueList.toString());

            // 응답 생성
            return new CommonResDto(HttpStatus.OK, 200, "친구 요청 조회를 완료했습니다.", valueList, links);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 에러 발생: " + e.getMessage(), null, links);
        }
    }

    // 요청한 친구 목록 조회
    public CommonResDto reqFriendsJoin() {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE"));

        try {      // 현재 사용자 이메일 가져오기
            String nickname = SecurityContextUtil.getCurrentUser().getNickname();


            //value 값으로 조회
            List<String> value = findKeysByValue(nickname);
            log.info(value.toString());
            // 응답 생성
            return new CommonResDto(HttpStatus.OK, 200, "친구 요청 조회를 완료했습니다.", value, links);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 에러 발생: " + e.getMessage(), null, links);
        }
    }
    public CommonResDto reqFriendsDelete(FriendsDto dto) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE"));
        try{
            String nickname = SecurityContextUtil.getCurrentUser().getNickname();
            List<String> valueList = findKeysByValue(nickname);
            log.info(valueList.toString());

            boolean flag = valueList.contains(dto.getResNickname());
            if (flag) {
                friendsTemplate.opsForSet().remove(dto.getResNickname(), nickname);
                return new CommonResDto(HttpStatus.OK, 200, "친구요청을 취소했습니다.", null, links);
            }
            return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "친구요청취소가 잘못되었습니다.", null, links);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 에러 발생: " + e.getMessage(), null, links);
        }
    }

    ////////////////////////////////// 서버 관리 ///////////////////////////////////////////////////////////////////////////////
//
//    // 서버 가입 신청
//    public CommonResDto addReqServer(ServerDto dto) {
//        List<CommonResDto.Link> links = new ArrayList<>();
//        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
//        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
//        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "Delete"));
//
//        try {
//            String email = SecurityContextUtil.getCurrentUser().getEmail();
//
//            // Redis에 서버 ID 추가
//            if (dto.getServerId() == null) {
//                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 서버 ID입니다.", null, links);
//            }
//
//            // Redis에 기존 값 확인
//            Set<String> existingServerIds = serverTemplate.opsForSet().members(email);
//            log.info(existingServerIds.toString());
//
//            if (existingServerIds != null && existingServerIds.contains(dto.getServerId())) {
//                return new CommonResDto(HttpStatus.OK, 200, "이미 가입이 진행 중입니다.", null, links);
//            }
//
//            // Redis에 서버 ID 추가
//            serverTemplate.opsForSet().add(email, dto.getServerId());
//
//            return new CommonResDto(HttpStatus.OK, 200, "서버 가입 요청이 완료되었습니다.", null, links);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 가입 요청 중 오류가 발생했습니다.", e.getMessage(), links);
//        }
//    }
//
//    // 서버 가입 응답
//    public CommonResDto addResServer(ServerDto dto) {
//        List<CommonResDto.Link> links = new ArrayList<>();
//        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
//        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
//        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE"));
//
//        try {
//            String email = SecurityContextUtil.getCurrentUser().getEmail();
//            log.info("Requester Email: {}", email);
//            log.info(dto.getAddStatus().toString());
//
//            // 사용자 조회
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));
//
//            // AddStatus 검증
//            if (dto.getAddStatus() == null) {
//                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "status를 결정해주세요 (APPROVE 또는 REJECT)", null, links);
//            }
//
//            // Redis에서 serverId 가져오기
//            Set<String> serverId = serverTemplate.opsForSet().members(email);
//            log.info(serverId.toString());
//
//
//            if (serverId == null || serverId.isEmpty()) {
//                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "해당 요청에 대한 serverId가 없습니다.", null, links);
//            }
//
//
//            if (dto.getAddStatus() == AddStatus.ACCEPT) {
//
//                //feign 통신을 통해서 Server-service로 전달.
//                FeignDto feignDto = new FeignDto();
//                feignDto.setServerId(serverId.toString());
//                feignDto.setUserId(user.getId());
//                feignClient.getUserIdByServerId(feignDto);
//
//                return new CommonResDto(HttpStatus.OK, 200, "서버 가입이 승인되었습니다.", null, links);
//
//            } else if (dto.getAddStatus() == AddStatus.REJECTED) {
//
//                // 서버 가입 요청 거절
//                deleteRedisServerData(email, serverId.toString());
//
//                return new CommonResDto(HttpStatus.OK, 200, "서버 가입을 거절하셨습니다.", null, links);
//            }
//
//            return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "Invalid AddStatus", null, links);
//        } catch (Exception e) {
//            log.error("Error in addResServer: {}", e.getMessage(), e);
//            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 응답 처리 중 오류 발생", e.getMessage(), links);
//        }
//    }
//
//
//    // 서버 가입 요청 조회
//    public CommonResDto addServerJoin(String serverId) {
//        List<CommonResDto.Link> links = new ArrayList<>();
//        links.add(new CommonResDto.Link("addServers", "/api/v1/users/servers", "POST"));
//        links.add(new CommonResDto.Link("ListServers", "/api/v1/users/servers", "GET"));
//        links.add(new CommonResDto.Link("DeleteServers", "/api/v1/users/servers", "DELETE"));
//
//        try {
//
//            // Redis에서 모든 요청 서버 ID 조회
//            List<String> keysByValue = findKeysByValue(serverId);
//
//
//            return new CommonResDto(HttpStatus.OK, 200, "가입 요청 서버 목록 조회 성공", keysByValue, links);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 요청 조회 중 오류 발생", null, links);
//        }
//    }
//
//
//    // Redis 데이터 삭제 로직
//    private void deleteRedisServerData(String email, String serverId) {
//        try {
//            serverTemplate.opsForSet().remove(email, serverId);
//            serverTemplate.opsForSet().remove(serverId, email);
//        } catch (Exception e) {
//            // Redis 삭제 실패 시 로그 추가
//            System.err.println("Redis 데이터 삭제 중 오류 발생: " + e.getMessage());
//        }
//    }
//
    // Redis에서 value로 조회하기 위한 전체 조회
    public List<String> findKeysByValue(String targetValue) {
        List<String> matchingKeys = new ArrayList<>(); // 결과를 저장할 리스트
        Set<String> keys = friendsTemplate.keys("*"); // 모든 키 가져오기

        if (keys != null) {
            for (String key : keys) {
                Set<String> values = friendsTemplate.opsForSet().members(key); // 각 키의 값 가져오기
                if (values != null && values.contains(targetValue)) {
                    matchingKeys.add(key); // 일치하는 키를 리스트에 추가
                }
            }
        }

        return matchingKeys; // 매칭된 키 리스트 반환
    }


}

