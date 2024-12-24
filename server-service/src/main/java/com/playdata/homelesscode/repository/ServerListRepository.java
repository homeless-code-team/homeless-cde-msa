package com.playdata.homelesscode.repository;

import com.playdata.homelesscode.entity.ServerList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServerListRepository extends JpaRepository<ServerList, String> {
    List<ServerList> findByUserId(String id);
}
