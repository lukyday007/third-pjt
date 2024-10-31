package com.singlebungle.backend.global.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jwt_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 사용자 ID
    private String accessToken; // 액세스 토큰
    private String refreshToken; // 리프레시 토큰
    private Long accessExpirationTime; // 액세스 토큰 만료 시간
    private Long refreshExpirationTime; // 리프레시 토큰 만료 시간
}