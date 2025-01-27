package com.spring.homelesscode.friends_server.repository;

import com.spring.homelesscode.friends_server.entity.AddStatus;
import com.spring.homelesscode.friends_server.entity.Friends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, String> {


    // FriendsRepository에서 두 이메일 필드로 id를 조회하는 쿼리
    List<Friends> findBySenderEmailOrReceiverEmailAndStatus(String senderEmail, String receiverEmail, AddStatus status);


    Optional<Friends> findByReceiverEmailAndSenderEmail(String receiverEmail, String senderEmail);

    List<Friends> findByReceiverEmailAndStatus(String receiverEmail, AddStatus status);

    List<Friends> findBySenderEmailAndStatus(String senderEmail, AddStatus status);

    boolean existsByReceiverEmailAndSenderEmail(String receiverEmail, String senderEmail);

}
