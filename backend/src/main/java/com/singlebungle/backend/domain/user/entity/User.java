package com.singlebungle.backend.domain.user.entity;

import com.singlebungle.backend.domain.user.dto.request.UserInfoRequestDTO;
import com.singlebungle.backend.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "nickname", nullable = true)
    private String nickname;

    @Column(name = "profile_image_path", nullable = true)
    private String profileImagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    public static User convertToEntity(UserInfoRequestDTO dto) {
        User user = new User();
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setProfileImagePath(""); // 기본값으로 빈 문자열 설정
        return user;
    }

    public enum Status {
        ACTIVE, INACTIVE, DELETED
    }
}