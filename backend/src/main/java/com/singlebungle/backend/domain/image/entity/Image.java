package com.singlebungle.backend.domain.image.entity;

import com.singlebungle.backend.domain.image.dto.request.ImageAppRequestDTO;
import com.singlebungle.backend.domain.image.dto.request.ImageWebRequestDTO;
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
@Table(name = "image")
public class Image extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", unique = true, nullable = false)
    private Long imageId;

    @Column(name = "image_url", nullable = false )
    private String imageUrl;

    @Column(name = "source_url", nullable = false)
    private String sourceUrl;

    @Column(name = "directory_id", nullable = false)
    private Long directoryId;

    private int count;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    // 웹에서 저장할 때
    public static Image convertToEntity(String sourceUrl, String imageUrl, Long directoryId) {
        Image image = new Image();
        image.setSourceUrl(sourceUrl);
        image.setImageUrl(imageUrl);
        image.setDirectoryId(directoryId);

        return image;
    }

}