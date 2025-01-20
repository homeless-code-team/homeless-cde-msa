package com.spring.homelesscode.friends_server.service;


import com.spring.homelesscode.friends_server.cofig.UserServiceClient;
import com.spring.homelesscode.friends_server.common.utill.SecurityContextUtil;
import com.spring.homelesscode.friends_server.dto.CommonResDto;
import com.spring.homelesscode.friends_server.dto.FeignResDto;
import com.spring.homelesscode.friends_server.dto.FriendsDto;
import com.spring.homelesscode.friends_server.entity.AddStatus;
import com.spring.homelesscode.friends_server.entity.Friends;
import com.spring.homelesscode.friends_server.repository.FriendsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FriendsService {

    private final FriendsRepository friendsRepository;
    private final SecurityContextUtil securityContextUtil;
    private final RedisTemplate<String, String> friendsTemplate;
    private final UserServiceClient userServiceclient;

    public FriendsService(FriendsRepository friendsRepository,
                          SecurityContextUtil securityContextUtil,
                          @Qualifier("friends") RedisTemplate<String, String> friendsTemplate,
                          UserServiceClient userServiceclient) {
        this.friendsRepository = friendsRepository;
        this.securityContextUtil = securityContextUtil;
        this.friendsTemplate = friendsTemplate;
        this.userServiceclient = userServiceclient;
    }

    // 친구 요청 처리 메서드
    public CommonResDto addFriends(FriendsDto dto) {
        List<CommonResDto.Link> links = List.of(
                new CommonResDto.Link("addFriends", "/api/v1/friends", "POST"),
                new CommonResDto.Link("ListFriends", "/api/v1/friends", "GET"),
                new CommonResDto.Link("DeleteFriends", "/api/v1/friends", "DELETE")
        );

        try {
            String senderEmail = SecurityContextUtil.getCurrentUser().getEmail();
            String receiverEmail = userServiceclient.getEmail(dto.getReceiverNickname());

            // 중복 검사
            boolean isDuplicate = friendsRepository.existsByReceiverEmailAndSenderEmail(receiverEmail, senderEmail)
                    || friendsRepository.existsByReceiverEmailAndSenderEmail(senderEmail, receiverEmail);

            if (isDuplicate) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "이미 진행 중인 요청 또는 친구 관계입니다", null, links);
            }

            // 새로운 친구 요청 생성
            Friends newFriendRequest = new Friends();
            newFriendRequest.setReceiverEmail(receiverEmail);
            newFriendRequest.setSenderEmail(senderEmail);
            newFriendRequest.setStatus(AddStatus.PENDING.name());

            friendsRepository.save(newFriendRequest);

            return new CommonResDto(HttpStatus.OK, 200, "친구 요청 완료", null, links);

        } catch (Exception e) {
            log.error("Error while adding friend request: {}", e.getMessage(), e);
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "에러 발생: " + e.getMessage(), null, links);
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
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            // JPQL로 친구 목록 조회 (refreshToken 여부 포함)
            List<Friends> results1 = friendsRepository.findByReceiverEmailAndStatus(email,AddStatus.ACCEPT.name());
            List<Friends> results2 = friendsRepository.findBySenderEmailAndStatus(email, AddStatus.ACCEPT.name());
            log.info(String.valueOf(results1));
            log.info(String.valueOf(results2));
            List<String> results = results1.stream()
                    .map(Friends::getSenderEmail) // Friends 객체에서 receiverEmail 필드 추출
                    .collect(Collectors.toList());
            List<String> results22 = results2.stream()
                    .map(Friends::getReceiverEmail) // Friends 객체에서 receiverEmail 필드 추출
                    .collect(Collectors.toList());

            //list를 합친다.
            results.addAll(results22);
            log.info(String.valueOf(results));
            // 친구 목록이 없을 경우 처리
            if (results.isEmpty()) {
                return new CommonResDto(HttpStatus.OK, 200, "친구 목록이 비어 있습니다.", Collections.emptyList(), links);
            }
            // refreshToken 확인 및 결과 생성 그리고 이메일을 닉네임으로 변환

            List<FeignResDto> friendList = userServiceclient.getUserDetails(results);
            log.info(String.valueOf(friendList));

            //친구목록을 사용자에게 반환
            return new CommonResDto(HttpStatus.OK, 200, "친구목록 조회 성공", friendList, links);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "에러발생" + e.getMessage(), null, links);
        }
    }

    // 친구 삭제
    public CommonResDto deleteFriend(String receiverNickname) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/friends", "Delete"));
        try {
            // 현재 사용자 정보 가져오기
            String email = securityContextUtil.getCurrentUser().getEmail();
            String friendEmail = userServiceclient.getEmail(receiverNickname);

            Friends byReceiverEmailAAndSenderEmail = friendsRepository.findByReceiverEmailAndSenderEmail(friendEmail, email)
                    .orElseThrow(() -> new UsernameNotFoundException("이미친구관계가 아닙니다: " + email));

            // 저장 삭제x
            friendsRepository.delete(byReceiverEmailAAndSenderEmail);

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
            String senderEmail = SecurityContextUtil.getCurrentUser().getEmail();
            log.info("senderEmail:{}",senderEmail);
            String receiverEmail = userServiceclient.getEmail(dto.getReceiverNickname());
            log.info("receiverNickname:{}",dto.getReceiverNickname());
            log.info("receiverEmail:{}",receiverEmail);


            // 이미 친구 관계 또는 요청 상태인지 확인
            Optional<Friends> friends = friendsRepository.findByReceiverEmailAndSenderEmail(senderEmail,receiverEmail);
            // 사용자 객체 로드
            boolean present = friends.isPresent();
            log.info(String.valueOf(present));
            if (present){
                if(friends.get().getStatus().equalsIgnoreCase(AddStatus.PENDING.name())){
                    if (dto.getAddStatus().equalsIgnoreCase(AddStatus.ACCEPT.name())){
                        friends.get().setStatus(AddStatus.ACCEPT.name());
                        friendsRepository.save(friends.get());
                        log.info("Friend relationship accepted");
                        return new CommonResDto(HttpStatus.OK, 200, "친구가 되었습니다.", null, links);
                    }else{
                        friendsRepository.delete(friends.get());
                        log.info("Friend relationship rejected");
                        return new CommonResDto(HttpStatus.OK, 200, "친구추가를 거절하셨습니다.", null, links);
                    }
                }else{
                    log.info("Friend relationship already exists");
                    return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "이미 친구입니다.", null, links);
                }
            }else{
                return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "ACCEPT 와 REJECT 중 하나를 선택해 주세요", null, links);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "잘못된 입력: " + e.getMessage(), null, links);

        } catch (Exception e) {
            log.error("Unexpected error during friend addition: {}", e.getMessage(), e);
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 에러 발생: " + e.getMessage(), null, links);
        }
    }

    // 사용자가 요청받은  친구 목록 조회
    public CommonResDto resFriendsJoin() {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE"));

        try {      // 현재 사용자 이메일 가져오기
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            List<Friends> byReceiverEmail1 = friendsRepository.findByReceiverEmailAndStatus(email, String.valueOf(AddStatus.PENDING));
            List<String> byReceiverEmail = byReceiverEmail1.stream()
                    .map(Friends::getSenderEmail) // Friends 객체에서 receiverEmail 필드 추출
                    .collect(Collectors.toList()); // List<String>으로 변환
            List<FeignResDto> friendList = userServiceclient.getUserDetails(byReceiverEmail);

            // 응답 생성
            return new CommonResDto(HttpStatus.OK, 200, "친구 요청 조회를 완료했습니다.", friendList, links);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 에러 발생: " + e.getMessage(), null, links);
        }
    }

    // 사용자가 요청한 친구 목록 조회
    public CommonResDto reqFriendsJoin() {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE"));

        try {
            // 현재 사용자 이메일 가져오기
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            List<Friends> bySenderEmail = friendsRepository.findBySenderEmailAndStatus(email, String.valueOf(AddStatus.PENDING));

            List<String> friendList1 = bySenderEmail.stream()
                    .map(Friends::getReceiverEmail) // Friends 객체에서 receiverEmail 필드 추출
                    .collect(Collectors.toList()); // List<String>으로 변환
            List<FeignResDto> friendList = userServiceclient.getUserDetails(friendList1);

            return new CommonResDto(HttpStatus.OK, 200, "친구 요청 조회를 완료했습니다.", friendList, links);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 에러 발생: " + e.getMessage(), null, links);
        }
    }



}

