package com.playdata.homelesscode.repository;

import com.playdata.homelesscode.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, String> {
    List<Board> findByChannelId(String id);
}
