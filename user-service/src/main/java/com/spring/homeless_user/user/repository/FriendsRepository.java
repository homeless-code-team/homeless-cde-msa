package com.spring.homeless_user.user.repository;

import com.spring.homeless_user.user.entity.Friends;
import com.spring.homeless_user.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, Long> {
    

    // 특정 사용자가 요청한 친구 목록
    List<Friends> findByUserEmail(String email);


    // 두 사용자 간의 친구 관계 조회
    Optional<Friends> findByUserEmailAndFriendEmail(String userEmail, String friendEmail);

}
