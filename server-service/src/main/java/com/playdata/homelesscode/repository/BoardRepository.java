package com.playdata.homelesscode.repository;

import com.playdata.homelesscode.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BoardRepository extends JpaRepository<Board, String> {

    Page<Board> findByBoardListIdOrderByCreateAtDesc(String id, Pageable pageable);

    Page<Board> findByBoardListIdAndTitleContainingOrderByCreateAtDesc(String boardId, String title, Pageable pageable);
}
