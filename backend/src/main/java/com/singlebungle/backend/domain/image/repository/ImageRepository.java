package com.singlebungle.backend.domain.image.repository;

import com.singlebungle.backend.domain.image.entity.Image;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    Image findBySourceUrlAndImageUrl(String webUrl, String imageUrl);

    boolean existsBySourceUrlAndImageUrl(@NotNull(message = "web url을 입력해주세요.") String webUrl, @NotNull(message = "image url을 입력해주세요.") String imageUrl);
}
