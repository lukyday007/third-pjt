package com.singlebungle.backend.domain.directory.repository;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Dictionary;
import java.util.List;
import java.util.Optional;

@Repository
public interface DirectoryRepository extends JpaRepository<Directory, Long> {

    // 특정 사용자에 대한 디렉토리를 순서대로 조회
    List<Directory> findAllByUserOrderByOrderAsc(User user);

    // 특정 사용자의 디렉토리 중 가장 높은 순서를 가진 디렉토리 조회
    @Query("SELECT COALESCE(MAX(d.order), 0) FROM Directory d WHERE d.user = :user")
    int findMaxOrderByUser(@Param("user") User user);

    // 사용자와 디렉토리 ID로 특정 디렉토리 조회
    Optional<Directory> findByDirectoryIdAndUser(Long directoryId, User user);

    boolean existsByName(String name);

    Optional<Directory> findByUserAndStatus(User user, int status);

    boolean existsByNameAndUser(String name, User user);
}
