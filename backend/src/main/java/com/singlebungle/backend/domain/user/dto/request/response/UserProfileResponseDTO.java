package com.singlebungle.backend.domain.user.dto.request.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Schema(title = "USER_PROFILE_RES : 회원 프로필 조회 응답 DTO")
public class UserProfileResponseDTO {

    @Schema(description = "회원 pk", example = "1")
    private Long userId;

    @Schema(description = "회원 닉네임", example = "nickname")
    private String nickname;

    @Schema(description = "이미지 url", example = "https://s3fweflf")
    private String profile;

    @Schema(description = "회원 이메일", example = "aaa@gmail.com")
    private String email;

    @Schema(description = "회원 생년월일", example = "2000/01/01")
    private LocalDate birth;

    @Schema(description = "회원 정보", example = "blabla")
    private String info;

    @Schema(description = "회원 경험치", example = "0")
    private int exp;

    @Schema(description = "사용자가 작성한 뉴지 총 개수", example = "0")
    private int newzyCnt;

    @Schema(description = "사용자를 팔로우하는 사람 수", example = "0")
    private int followerCnt;

    @Schema(description = "사용자가 팔로우하는 사람 수", example = "0")
    private int followingCnt;

}
