package com.playdata.homelesscode.repository;

import com.playdata.homelesscode.entity.Board;
import com.playdata.homelesscode.entity.BoardList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface BoardRepository extends JpaRepository<Board, String> {

    Page<Board> findByBoardListIdOrderByCreateAtDesc(String id, Pageable pageable);

    Page<Board> findByBoardListIdAndTitleContainingOrderByCreateAtDesc(String boardId, String title, Pageable pageable);

    List<Board> findByBoardListId(String boardId);
}
