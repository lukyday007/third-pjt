package com.singlebungle.backend.domain.image.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(title="IMAGE_INFO_REQ : 이미지 목록 요청 DTO")
public class ImageListGetRequestDTO {

    private Long userId;
    private Long directoryId;
    private int page;
    private int size;
    private String keyword;
    private int sort;
    private Boolean isBin;

    public ImageListGetRequestDTO(Long userId, int page, int size, String keyword, int sort) {
        this.userId = userId;
        this.page = page;
        this.size = size;
        this.keyword = keyword;
        this.sort = sort;
    }

    public ImageListGetRequestDTO(Long userId, int page, int size, String keyword, int sort, Long directoryId) {
        this.userId = userId;
        this.page = page;
        this.size = size;
        this.keyword = keyword;
        this.sort = sort;
        this.directoryId = directoryId;
    }

    public ImageListGetRequestDTO(int page, int size, String keyword, int sort, Boolean isBin) {
        this.userId = userId;
        this.page = page;
        this.size = size;
        this.keyword = keyword;
        this.sort = sort;
        this.isBin = isBin;
    }

}
