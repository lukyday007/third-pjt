package com.singlebungle.backend.domain.image.repository;

import com.singlebungle.backend.domain.image.entity.ImageDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageDetailRepository extends JpaRepository<ImageDetail, Long> {

}
