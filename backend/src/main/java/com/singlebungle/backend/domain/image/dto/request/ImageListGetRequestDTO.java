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

    private Long directoryId;
    private int page;
    private int size;
    private String keyword;
    private int sort;

    public ImageListGetRequestDTO(int page, int size, String keyword, int sort) {
        this.page = page;
        this.size = size;
        this.keyword = keyword;
        this.sort = sort;
    }

}
