package com.playdata.homelesscode.repository;

import com.playdata.homelesscode.entity.BoardList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardListRepository extends JpaRepository<BoardList, String> {
    List<BoardList> findByServerId(String id);
}
