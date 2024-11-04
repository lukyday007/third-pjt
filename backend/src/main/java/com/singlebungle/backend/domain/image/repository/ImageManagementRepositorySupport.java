package com.singlebungle.backend.domain.image.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.singlebungle.backend.domain.image.dto.request.ImageListGetRequestDTO;
import com.singlebungle.backend.domain.image.dto.response.ImageListGetResponseDTO;
import com.singlebungle.backend.domain.image.entity.ImageManagement;
import com.singlebungle.backend.domain.image.entity.QImage;
import com.singlebungle.backend.domain.image.entity.QImageDetail;
import com.singlebungle.backend.domain.image.entity.QImageManagement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class ImageManagementRepositorySupport extends QuerydslRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public ImageManagementRepositorySupport(JPAQueryFactory queryFactory) {
        super(ImageManagement.class);
        this.queryFactory = queryFactory;
    }

    public Map<String, Object> findImageList(ImageListGetRequestDTO requestDTO) {

        /*
            private Long directoryId;
            private int page;
            private int size;
            private String keyword;
            private int sort;
        */

        QImage qImage = QImage.image;
        QImageDetail qImageDetail = QImageDetail.imageDetail;
        QImageManagement qImageManagement = QImageManagement.imageManagement;

        // directoryId 필터링
        BooleanBuilder builder = new BooleanBuilder();
        if (requestDTO.getDirectoryId() != null)
            builder.and(qImageManagement.directory.directoryId.eq(requestDTO.getDirectoryId()));

        // 키워드 검색
        if (requestDTO.getKeyword() != null && !requestDTO.getKeyword().isEmpty()) {
            builder.and(qImageDetail.keyword.keywordName.containsIgnoreCase(requestDTO.getKeyword()));  // containsIgnoreCase : 대소문자를 구분하지 않고 해당 글자를 포함할 때
        }

        JPAQuery<ImageListGetResponseDTO> query = queryFactory
                .select(Projections.constructor(ImageListGetResponseDTO.class,
                        QImageManagement.imageManagement.imageManagementId,
                        QImageManagement.imageManagement.image.imageUrl
                        ))
                .from(qImageManagement)
                .leftJoin(qImageManagement.image, qImage)
                .where(builder);

        // 정렬 조건문
        if (requestDTO.getSort() == 0) {
            query.orderBy(qImageManagement.createdAt.desc());  // 조회수 순 정렬
            log.info(">>> 정렬 조건: 최신 순");
        } else if (requestDTO.getSort() == 1) {
            query.orderBy(qImageManagement.createdAt.asc());  // 오래된 순 정렬
            log.info(">>> 정렬 조건: 오래된 순");
        } else if (requestDTO.getSort() == 2) {
            query.orderBy(Expressions.numberTemplate(Double.class, "RAND()").asc());
            log.info(">>> 정렬 조건: 랜덤 순");
        }

        List<ImageListGetResponseDTO> imageList = query
                .offset((requestDTO.getPage() - 1) * requestDTO.getSize())
                .limit(requestDTO.getSize())
                .fetch();

        Long totalCount = query.fetchCount();
        // 페이지 세기 && 사진 갯수
        int totalPage = (int) ((totalCount + requestDTO.getSize() - 1) / requestDTO.getSize());

        Map<String, Object> result = new HashMap<>();
        result.put("image-list", imageList);
        result.put("total-page", totalPage);

        return result;
    }

}
