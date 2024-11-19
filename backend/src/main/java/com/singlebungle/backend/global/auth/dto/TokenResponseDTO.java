package com.singlebungle.backend.global.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(title = "TOKEN_RES : JWT 토큰 반환 DTO")
public class TokenResponseDTO {
    private String accessToken;
    private String refreshToken;
}
