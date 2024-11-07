package com.singlebungle.backend.domain.image.repository;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.entity.ImageManagement;
import com.singlebungle.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageManagementRepository extends JpaRepository<ImageManagement, Long> {
    void deleteByCurDirectory(Directory curDirectory);
    List<ImageManagement> findByUserAndImageIn(User user, List<Image> images);

    List<ImageManagement> findByCurDirectory(Directory directory);
}
