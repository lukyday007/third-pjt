package com.singlebungle.backend.global.auth.repository;

import com.singlebungle.backend.global.auth.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    Optional<JwtToken> findByUserId(Long userId);// 사용자 ID로 JWT 토큰 조회
    Optional<JwtToken> findByUserIdAndRedirectUri(Long userId, String redirectUri);
    Optional<JwtToken> findByUserIdAndAccessToken(Long userId, String accessToken);

    void deleteByUserId(Long userId);
    void deleteByAccessToken(String token);

    Optional<JwtToken> findByUserIdAndRefreshToken(Long userId, String refreshToken);
}

