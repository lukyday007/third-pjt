package com.singlebungle.backend.domain.user.dto.response;

import com.singlebungle.backend.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Schema(title = "USER_INFO_RES : 회원 등록 응답 DTO")
public class UserInfoResponseDTO {

    @Schema(description = "회원 pk", example = "1")
    private Long userId;

    @Schema(description = "회원 닉네임", example = "nickname")
    private String nickname;

    @Schema(description = "회원 이메일", example = "aaa@gmail.com")
    private String email;

    @Schema(description = "회원 프로필 사진 경로", example = "")
    private String profileImagePath;

    public static UserInfoResponseDTO convertToDTO(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User 엔티티 값이 없습니다.");
        }
        return UserInfoResponseDTO.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImagePath(user.getProfileImagePath())
                .email(user.getEmail())
                .build();
    }

}
