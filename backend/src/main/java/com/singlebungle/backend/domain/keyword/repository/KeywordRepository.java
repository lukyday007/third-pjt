package com.singlebungle.backend.domain.keyword.repository;

import com.singlebungle.backend.domain.keyword.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    boolean existsByKeywordName(String keyword);

    Keyword findByKeywordName(String keyword);
}
