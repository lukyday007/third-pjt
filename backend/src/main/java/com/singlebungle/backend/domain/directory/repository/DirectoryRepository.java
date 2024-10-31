package com.singlebungle.backend.domain.directory.repository;

import com.singlebungle.backend.domain.directory.entity.Directory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Dictionary;

@Repository
public interface DirectoryRepository extends JpaRepository<Directory, Long> {

    boolean existsByName(String name);
}
