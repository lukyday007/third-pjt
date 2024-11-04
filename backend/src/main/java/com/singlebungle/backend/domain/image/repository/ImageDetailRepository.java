package com.singlebungle.backend.domain.image.repository;

import com.singlebungle.backend.domain.image.dto.request.ImageListGetRequestDTO;
import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.entity.ImageDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ImageDetailRepository extends JpaRepository<ImageDetail, Long> {

    List<ImageDetail> findAllByImage(Image image);

    void deleteByImageDetailIdIn(List<Long> imageDetailIds);
}
