package com.singlebungle.backend.domain.image.repository;

import com.singlebungle.backend.domain.image.entity.Image;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findBySourceUrlAndImageUrl(String webUrl, String imageUrl);

    boolean existsByImageUrl(String imageUrl);

    Image findByImageUrl(String imageUrl);
}
