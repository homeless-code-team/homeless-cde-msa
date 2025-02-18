package com.spring.homeless_user.user.repository;

import com.netflix.appinfo.ApplicationInfoManager;
import com.spring.homeless_user.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String > {


    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmail(String email);

    void deleteByEmail(String email);

    Boolean existsByEmail(String email);

    List<User> findByEmailIn(List<String> userEmails);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmailAndProvider(String email, String registrationId);
}
