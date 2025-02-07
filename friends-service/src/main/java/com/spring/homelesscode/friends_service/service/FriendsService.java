package com.spring.homelesscode.friends_service.service;


import com.spring.homelesscode.friends_service.common.dto.UserResponseDto;
import com.spring.homelesscode.friends_service.config.UserServiceClient;
import com.spring.homelesscode.friends_service.common.utill.SecurityContextUtil;
import com.spring.homelesscode.friends_service.dto.CommonResDto;
import com.spring.homelesscode.friends_service.dto.FeignResDto;
import com.spring.homelesscode.friends_service.dto.FriendsDto;
import com.spring.homelesscode.friends_service.entity.AddStatus;
import com.spring.homelesscode.friends_service.entity.Friends;
import com.spring.homelesscode.friends_service.repository.FriendsRepository;
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
    private final UserServiceClient userServiceclient;

    public FriendsService(FriendsRepository friendsRepository,
                          SecurityContextUtil securityContextUtil,
                          @Qualifier("friends") RedisTemplate<String, String> friendsTemplate,
                          UserServiceClient userServiceclient) {
        this.friendsRepository = friendsRepository;
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

            boolean isDuplicate = friendsRepository.existsByReceiverEmailAndSenderEmail(receiverEmail, senderEmail)
                    || friendsRepository.existsByReceiverEmailAndSenderEmail(senderEmail, receiverEmail);

            if (isDuplicate) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "이미 진행 중인 요청 또는 친구 관계입니다", null, links);
            }

            Friends newFriendRequest = new Friends();
            newFriendRequest.setReceiverEmail(receiverEmail);
            newFriendRequest.setSenderEmail(senderEmail);
            newFriendRequest.setStatus(AddStatus.PENDING);

            friendsRepository.save(newFriendRequest);

            return new CommonResDto(HttpStatus.OK, 200, "친구 요청 완료", null, links);

        } catch (Exception e) {
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "에러 발생: " + e.getMessage(), null, links);
        }
    }


    public CommonResDto getFriends() {
        // REST API 링크 설정 (추가, 조회, 삭제 등)
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/friends", "DELETE"));

        try {
            // Step 1. 양방향 친구 조회
            String currentUserEmail = SecurityContextUtil.getCurrentUser().getEmail();
            List<Friends> senderFriends = friendsRepository.findBySenderEmailAndStatus(currentUserEmail, AddStatus.ACCEPT);
            List<Friends> receiverFriends = friendsRepository.findByReceiverEmailAndStatus(currentUserEmail, AddStatus.ACCEPT);

            // Step 2. 친구가 없는 경우 예외 처리
            if (senderFriends.isEmpty() && receiverFriends.isEmpty()) {
                return new CommonResDto(
                        HttpStatus.OK,
                        200,
                        "친구 목록이 비어 있습니다.",
                        Collections.emptyList(),
                        links
                );
            }

            // Step 3. 친구 정보 받아오기 및 Step 4. Friends 엔티티의 id와 결합하여 DTO 생성
            List<UserResponseDto> friendInfoList = new ArrayList<>();

            // (1) 내가 친구 요청을 보낸 경우
            for (Friends friendRelation : senderFriends) {
                String friendEmail = friendRelation.getReceiverEmail();
                UserResponseDto friendUserInfo = userServiceclient.findFriendByEmail(friendEmail);
                UserResponseDto dto = UserResponseDto.builder()
                        .id(friendRelation.getId())  // Friends 엔티티의 id
                        .email(friendUserInfo.getEmail())
                        .nickname(friendUserInfo.getNickname())
                        .profileImage(friendUserInfo.getProfileImage())
                        .build();
                friendInfoList.add(dto);
            }

            // (2) 내가 친구 요청을 받은 경우
            for (Friends friendRelation : receiverFriends) {
                String friendEmail = friendRelation.getSenderEmail();
                UserResponseDto friendUserInfo = userServiceclient.findFriendByEmail(friendEmail);
                UserResponseDto dto = UserResponseDto.builder()
                        .id(friendRelation.getId())  // Friends 엔티티의 id
                        .email(friendUserInfo.getEmail())
                        .nickname(friendUserInfo.getNickname())
                        .profileImage(friendUserInfo.getProfileImage())
                        .build();
                friendInfoList.add(dto);
            }

            // 최종 친구 목록 반환
            return new CommonResDto(
                    HttpStatus.OK,
                    200,
                    "친구목록 조회 성공",
                    friendInfoList,
                    links
            );
        } catch (Exception e) {
            return new CommonResDto(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    500,
                    "에러발생: " + e.getMessage(),
                    null,
                    links
            );
        }
    }



    // 친구 삭제
    public CommonResDto deleteFriend(String receiverNickname) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/friends", "DELETE"));

        try {
            // 1. 현재 로그인한 사용자 이메일 가져오기
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            // 2. 친구 닉네임을 이용해 친구 이메일 가져오기
            String friendEmail = userServiceclient.getEmail(receiverNickname);

            // 3. 친구 관계 조회 (내가 보낸 요청 or 내가 받은 요청 둘 다 체크)
            Optional<Friends> friendship = friendsRepository.findByReceiverEmailAndSenderEmail(friendEmail, email);
            if (friendship.isEmpty()) {
                friendship = friendsRepository.findByReceiverEmailAndSenderEmail(email, friendEmail);
            }

            // 4. 친구 관계가 없으면 예외 처리
            Friends friend = friendship.orElseThrow(() ->
                    new UsernameNotFoundException("삭제할 친구 관계를 찾을 수 없습니다.")
            );

            // 5. 친구 관계 삭제
            friendsRepository.delete(friend);

            return new CommonResDto(HttpStatus.OK, 200, "친구 관계가 삭제되었습니다.", null, links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "에러 발생: " + e.getMessage(), null, links);
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
            String receiverEmail = userServiceclient.getEmail(dto.getReceiverNickname());

            Optional<Friends> friends = friendsRepository.findByReceiverEmailAndSenderEmail(senderEmail, receiverEmail);
            boolean present = friends.isPresent();
            if (present) {
                if (friends.get().getStatus().equalsIgnoreCase(AddStatus.PENDING.name())) {
                    if (dto.getAddStatus().equalsIgnoreCase(AddStatus.ACCEPT.name())) {
                        friends.get().setStatus(AddStatus.ACCEPT);
                        friendsRepository.save(friends.get());
                        log.info("Friend relationship accepted");
                        return new CommonResDto(HttpStatus.OK, 200, "친구가 되었습니다.", null, links);
                    } else {
                        friendsRepository.delete(friends.get());
                        log.info("Friend relationship rejected");
                        return new CommonResDto(HttpStatus.OK, 200, "친구추가를 거절하셨습니다.", null, links);
                    }
                } else {
                    log.info("Friend relationship already exists");
                    return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "이미 친구입니다.", null, links);
                }
            } else {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "ACCEPT 와 REJECT 중 하나를 선택해 주세요", null, links);
            }
        } catch (IllegalArgumentException e) {
            return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "잘못된 입력: " + e.getMessage(), null, links);

        } catch (Exception e) {
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

            List<Friends> byReceiverEmail1 = friendsRepository.findByReceiverEmailAndStatus(email, AddStatus.PENDING);
            List<String> byReceiverEmail = byReceiverEmail1.stream()
                    .map(Friends::getSenderEmail)
                    .collect(Collectors.toList());
            List<FeignResDto> friendList = userServiceclient.getUserDetails(byReceiverEmail);

            log.info(friendList.toString());
            // 응답 생성
            return new CommonResDto(HttpStatus.OK, 200, "친구 요청 조회를 완료했습니다.", friendList, links);
        } catch (Exception e) {
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
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            List<Friends> bySenderEmail = friendsRepository.findBySenderEmailAndStatus(email, AddStatus.PENDING);

            List<String> friendList1 = bySenderEmail.stream()
                    .map(Friends::getReceiverEmail)
                    .collect(Collectors.toList());
            List<FeignResDto> friendList = userServiceclient.getUserDetails(friendList1);
            log.info(friendList.toString());

            return new CommonResDto(HttpStatus.OK, 200, "친구 요청 조회를 완료했습니다.", friendList, links);
        } catch (Exception e) {
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 에러 발생: " + e.getMessage(), null, links);
        }
    }


}

