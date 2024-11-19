package com.singlebungle.backend.domain.user.dto.request.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "USER_FIRSTLOGIN_RES : 유저 첫 로그인 반환 DTO")
public class UserFirstLoginResponseDTO {
    private boolean isFirstLogin;
}