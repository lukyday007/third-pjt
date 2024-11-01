package com.singlebungle.backend.global.auth.entity;

import com.singlebungle.backend.global.model.BaseTimeEntity;
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
public class JwtToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 사용자 ID
    private String accessToken; // 액세스 토큰
    private String refreshToken; // 리프레시 토큰
    private Long accessExpirationTime; // 액세스 토큰 만료 시간
    private Long refreshExpirationTime; // 리프레시 토큰 만료 시간
    private String redirectUri; // 리다이렉트 URI
}