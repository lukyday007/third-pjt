package com.singlebungle.backend.domain.image.repository;

import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.entity.ImageDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageDetailRepository extends JpaRepository<ImageDetail, Long> {

    List<ImageDetail> findAllByImage(Image image);

    @Query("SELECT id.keyword.keywordName FROM ImageDetail id " +
            "WHERE id.image.imageId IN :imageIds " +
            "AND id.keyword.keywordName LIKE %:keyword%")
    List<String> findKeywordsByImageIdsAndKeyword(List<Long> imageIds, String keyword);
}
