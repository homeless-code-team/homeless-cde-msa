package com.spring.homeless_user.user.repository;

import com.spring.homeless_user.user.entity.Friends;
import com.spring.homeless_user.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, Long> {
    List<Friends> findAllById(Long userId);

    Optional<Friends> findByUserAndFriend(User reqUser, User resUser);
}
