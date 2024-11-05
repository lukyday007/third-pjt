package com.singlebungle.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "AuthRequestDTO : 소셜 로그인 요청 DTO")
public class AuthRequestDTO {
    String email;
    String nickname;
    String profileImagePath;
    Long id;
}