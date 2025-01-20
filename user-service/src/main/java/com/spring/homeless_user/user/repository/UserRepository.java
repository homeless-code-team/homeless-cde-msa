package com.spring.homeless_user.user.repository;

import com.spring.homeless_user.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmail(String email);

    void deleteByEmail(String email);

    List<User> findByEmailIn(List<String> userEmails);
}
