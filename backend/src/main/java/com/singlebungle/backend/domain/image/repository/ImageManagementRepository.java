package com.singlebungle.backend.domain.image.repository;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.entity.ImageManagement;
import com.singlebungle.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageManagementRepository extends JpaRepository<ImageManagement, Long> {

    void deleteByCurDirectory(Directory curDirectory);

    List<ImageManagement> findByCurDirectory(Directory directory);

    @Query("SELECT im.image.imageId FROM ImageManagement im WHERE im.curDirectory IN :directories")
    List<Long> findImageIdsByDirectories(List<Directory> directories);
}
