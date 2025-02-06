package com.spring.homelesscode.friends_service.repository;

import com.spring.homelesscode.friends_service.entity.AddStatus;
import com.spring.homelesscode.friends_service.entity.Friends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, String> {


    Optional<Friends> findByReceiverEmailAndSenderEmail(String receiverEmail, String senderEmail);

    List<Friends> findByReceiverEmailAndStatus(String receiverEmail, AddStatus status);

    List<Friends> findBySenderEmailAndStatus(String senderEmail, AddStatus status);

    boolean existsByReceiverEmailAndSenderEmail(String receiverEmail, String senderEmail);

}
