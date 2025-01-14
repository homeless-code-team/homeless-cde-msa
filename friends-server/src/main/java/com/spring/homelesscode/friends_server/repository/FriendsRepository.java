package com.spring.homelesscode.friends_server.repository;

import com.spring.homelesscode.friends_server.entity.Friends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, Long> {

    List<Friends> findByNickname(String resNickname);

    // 특정 사용자가 요청한 친구 목록

}
