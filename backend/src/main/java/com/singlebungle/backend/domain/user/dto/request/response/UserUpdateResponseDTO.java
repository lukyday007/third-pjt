package com.singlebungle.backend.domain.user.dto.request.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "USER_UPDATE_RES : 유저 수정 반환 DTO")
public class UserUpdateResponseDTO {

    @Schema(description = "유저 닉네임", example = "nickname")
    private Long userId;
    @Schema(description = "유저 닉네임", example = "nickname")
    private String nickname;
    @Schema(description = "유저 이메일", example = "abc@gmail.com")
    private String email;
    @Schema(description = "유저 생년월일", example = "2000/01/01")
    private LocalDate birth;
    @Schema(description = "유저 정보", example = "blabla")
    private String info;
    @Schema(description = "소셜 로그인 타입", example = "kakao")
    private String socialLoginType;
}
