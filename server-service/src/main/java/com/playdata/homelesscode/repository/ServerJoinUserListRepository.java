package com.playdata.homelesscode.repository;

import com.playdata.homelesscode.entity.ServerJoinUserList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServerJoinUserListRepository extends JpaRepository<ServerJoinUserList, String> {


    List<ServerJoinUserList> findByEmail(String email);

    void deleteByServerIdAndEmail(String id, String userEmail);

    List<ServerJoinUserList> findByServerId(String id);


    ServerJoinUserList findByEmailAndServerId(String email, String id);
}
