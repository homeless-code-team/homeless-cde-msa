package com.spring.homelesscode.friends_server.service;


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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
public class FriendsAndServerService {

    private final FriendsRepository friendsRepository;
    private final SecurityContextUtil securityContextUtil;
    private final RedisTemplate<String, String> friendsTemplate;
    private final RedisTemplate<String, String> serverTemplate;

    public FriendsAndServerService(FriendsRepository friendsRepository,
                                   SecurityContextUtil securityContextUtil,
                                   @Qualifier("friends") RedisTemplate<String, String> friendsTemplate,
                                   @Qualifier("server") RedisTemplate<String, String> serverTemplate) {
        this.friendsRepository = friendsRepository;
        this.securityContextUtil = securityContextUtil;
        this.friendsTemplate = friendsTemplate;
        this.serverTemplate = serverTemplate;
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
            String nickname = securityContextUtil.getCurrentUser().getNickname();

            List<Friends> friends = friendsRepository.findByNickname(nickname);

            // 친구 목록이 없을 경우 처리
            if (friends == null || friends.isEmpty()) {
                return new CommonResDto(HttpStatus.OK, 200, "친구 목록이 비어 있습니다.", Collections.emptyList(), links);
            }
            //친구목록을 사용자에게 반환
            return new CommonResDto(HttpStatus.OK, 200, "친구목록 조회 성공", friends, links);
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

