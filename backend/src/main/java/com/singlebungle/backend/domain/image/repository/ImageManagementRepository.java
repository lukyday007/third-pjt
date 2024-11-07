package com.singlebungle.backend.domain.image.repository;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.image.entity.ImageManagement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageManagementRepository extends JpaRepository<ImageManagement, Long> {
    void deleteByCurDirectory(Directory curDirectory);
}
