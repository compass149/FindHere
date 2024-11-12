package com.projectdemo1.repository;

import com.projectdemo1.board4.domain.Creply;
import com.projectdemo1.domain.Board;
import com.projectdemo1.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select c from Comment c where c.board.bno = :bno")
    Page<Comment> listOfBoard(Long bno, Pageable pageable);

    void deleteByBoardBno(Long bno);
}
