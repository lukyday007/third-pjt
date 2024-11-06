package com.singlebungle.backend.domain.image.entity;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.user.entity.User;
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
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 이미지 번호
    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    // 현재 디렉토리 번호 (활성화)
    @ManyToOne
    @JoinColumn(name = "cur_directory_id", nullable = false)
    private Directory curDirectory;

    // 이전 디렉토리 번호 (비활성화)
    @ManyToOne
    @JoinColumn(name = "prev_directory_id")
    private Directory prevDirectory;



    // 엔티티 생성
    public static ImageManagement convertToEntity (User user, Image image, Directory directory) {
        ImageManagement imageManagement = new ImageManagement();
        imageManagement.setUser(user);
        imageManagement.setImage(image);
        imageManagement.setCurDirectory(directory);

        return imageManagement;
    }

    // 이미지 폴더 이동
    public static ImageManagement converToEntity(User user, Image image, Directory curDirectory, Directory prevDirectory) {
        ImageManagement imageManagement = new ImageManagement();
        imageManagement.setUser(user);
        imageManagement.setImage(image);
        imageManagement.setCurDirectory(curDirectory);
        imageManagement.setPrevDirectory(prevDirectory);

        return imageManagement;
    }



}
