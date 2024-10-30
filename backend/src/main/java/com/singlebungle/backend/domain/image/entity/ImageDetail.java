package com.singlebungle.backend.domain.image.entity;

import com.singlebungle.backend.domain.keyword.entity.Keyword;
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
@Table(name = "image_detail")
public class ImageDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_detail_id", nullable = false)
    private Long imageDetailId;

    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @ManyToOne
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;



    public static ImageDetail convertToEntity(Image image, Keyword keyword) {
        ImageDetail imageDetail = new ImageDetail();
        imageDetail.setImage(image);
        imageDetail.setKeyword(keyword);

        return imageDetail;
    }

}
