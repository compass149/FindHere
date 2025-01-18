package com.projectdemo1.board4.repository;


import com.projectdemo1.board4.domain.Cboard;
import com.projectdemo1.board4.repository.search.CboardSearch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CboardRepository extends JpaRepository<Cboard, Long>, CboardSearch {

    @EntityGraph(attributePaths = {"imageSet"})
    @Query("select b from Cboard b where b.cno = :cno")
    Optional<Cboard> findByIdWithImages(Long cno);
}