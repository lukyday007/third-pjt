package com.singlebungle.backend.domain.image.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title="IMAGE_INFO_REQ : 이미지 목록 요청 DTO")
public class ImageListGetRequestDTO {

    private Long userId;

    @Builder.Default
    private Long directoryId = 0L;      // 중요

    private int page;
    private int size;
    private List<String> keywords;      // 중요
    private int sort;
    private boolean isBin;              // 중요

    // getImageListFromFeed
    public ImageListGetRequestDTO(int page, int size, List<String> keywords, int sort) {
        this.page = page;
        this.size = size;
        this.keywords = keywords;
        this.sort = sort;
    }

    // getImageListFromFeed
    public ImageListGetRequestDTO(int page, int size, int sort) {
        this.page = page;
        this.size = size;
        this.sort = sort;
    }


    // getImageListFromDirectory
    public ImageListGetRequestDTO(Long userId, Long directoryId, int page, int size,  int sort, boolean isBin) {
        this.userId = userId;
        this.page = page;
        this.size = size;
        this.sort = sort;
        this.directoryId = directoryId;
        this.isBin = isBin;
    }


}