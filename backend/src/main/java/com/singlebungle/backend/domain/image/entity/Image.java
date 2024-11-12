package com.singlebungle.backend.domain.image.entity;

import com.singlebungle.backend.domain.image.dto.request.ImageAppRequestDTO;
import com.singlebungle.backend.domain.image.dto.request.ImageWebRequestDTO;
import com.singlebungle.backend.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "image")
@Builder
public class Image extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", unique = true, nullable = false)
    private Long imageId;

    @Column(name = "image_url", nullable = false )
    private String imageUrl;

    @Column(name = "source_url", columnDefinition = "TEXT", nullable = false)
    private String sourceUrl;

    @Builder.Default
    private int count = 1;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    // 웹에서 저장할 때
    public static Image convertToEntity(String sourceUrl, String imageUrl) {
        Image image = new Image();
        image.setSourceUrl(sourceUrl);
        image.setImageUrl(imageUrl);

        return image;
    }

}