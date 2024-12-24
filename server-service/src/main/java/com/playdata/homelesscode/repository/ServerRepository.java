package com.playdata.homelesscode.repository;

import com.playdata.homelesscode.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServerRepository extends JpaRepository<Server, String> {
    List<Server> findByIdIn(List<String> collect);
}
