package com.singlebungle.backend.domain.image.entity;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "image_management")
public class ImageManagement extends BaseTimeEntity {  // 디렉토리 목록 용

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_management_id", unique = true, nullable = false)
    private Long imageManagementId;

//    // 유저 번호
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    // 이미지 번호
    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    // 현재 디렉토리 번호 (활성화)
    @ManyToOne
    @JoinColumn(name = "directory_id", nullable = false)
    private Directory directory;

    // 이전 디렉토리 번호 (비활성화)


    // 엔티티 생성
    public static ImageManagement convertToEntity (Image image, Directory directory) {
        ImageManagement imageManagement = new ImageManagement();
        imageManagement.setImage(image);
        imageManagement.setDirectory(directory);

        return imageManagement;
    }



}
