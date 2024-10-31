package com.singlebungle.backend.domain.user.repository;

import com.singlebungle.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByNickname(String nickname);
    Optional<User> findUserByEmail(String email);
    Optional<User> findByUserId(Long userId);
    User findByNickname(String nickname);
}
