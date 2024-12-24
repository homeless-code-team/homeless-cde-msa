package com.spring.homeless_user.user.repository;


import com.spring.homeless_user.user.entity.Servers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRepository extends JpaRepository<Servers, Long> {
}
