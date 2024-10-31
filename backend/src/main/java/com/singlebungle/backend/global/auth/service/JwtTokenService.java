package com.singlebungle.backend.global.auth.service;

import com.singlebungle.backend.global.auth.entity.JwtToken;
import com.singlebungle.backend.global.auth.repository.JwtTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final JwtTokenRepository jwtTokenRepository;

    // 사용자 ID로 모든 JWT 토큰 삭제
    public void deleteAllTokens(Long userId) {
        jwtTokenRepository.deleteByUserId(userId);
    }

    // 새로운 JWT 토큰 저장
    public void saveJwtToken(JwtToken jwtToken) {
        jwtTokenRepository.save(jwtToken);
    }
}

