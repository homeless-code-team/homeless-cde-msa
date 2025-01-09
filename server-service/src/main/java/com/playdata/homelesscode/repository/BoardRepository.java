package com.playdata.homelesscode.repository;

import com.playdata.homelesscode.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    List<Board> findByBoardListId(String id);
}
