package com.singlebungle.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(title = "USER_INFO_REQ : 유저 등록 요청 DTO")
public class UserInfoRequestDTO {

    @NotNull(message = "닉네임을 입력해주세요.")
    @Schema(description = "유저 닉네임", example = "nickname")
    private String nickname;

    @NotNull(message = "이메일을 입력해주세요.")
    @Schema(description = "유저 이메일", example = "abc@gmail.com")
    private String email;


}
