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
import com.singlebungle.backend.domain.keyword.entity.QKeyword;
import com.singlebungle.backend.domain.user.entity.QUser;
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

    public Map<String, Object> findImageListFromDir(ImageListGetRequestDTO requestDTO) {

        QUser qUser = QUser.user;
        QImage qImage = QImage.image;
        QImageDetail qImageDetail = QImageDetail.imageDetail;
        QImageManagement qImageManagement = QImageManagement.imageManagement;
        QKeyword qKeyword = QKeyword.keyword;

        BooleanBuilder builder = new BooleanBuilder();

        // 유저 정보 필터링 - User 엔티티를 조인하여 userId 조건 추가
        builder.and(qImageManagement.user.userId.eq(requestDTO.getUserId()));

        // directoryId 처리
        boolean status = requestDTO.isBin();
        if (status) {
            builder.and(qImageManagement.curDirectory.status.eq(2)); // 휴지통
        } else {
            if (requestDTO.getDirectoryId() == null || requestDTO.getDirectoryId() == 0) {
                builder.and(qImageManagement.curDirectory.status.eq(0));
            } else if (requestDTO.getDirectoryId().equals(-1L)) {
                builder.and(qImageManagement.curDirectory.status.ne(2));
            } else {
                builder.and(qImageManagement.curDirectory.directoryId.eq(requestDTO.getDirectoryId()));
            }
        }

        // 키워드를 OR 조건으로 추가
        if (requestDTO.getKeywords() != null && !requestDTO.getKeywords().isEmpty()) {
            BooleanBuilder keywordBuilder = new BooleanBuilder();

            for (String keyword : requestDTO.getKeywords()) {
                keywordBuilder.or(qImageDetail.keyword.keywordName.containsIgnoreCase(keyword));
            }
            builder.and(keywordBuilder);
        }

        JPAQuery<ImageListGetResponseDTO> query = queryFactory
                .select(Projections.constructor(ImageListGetResponseDTO.class,
                        qImageManagement.imageManagementId,
                        qImageManagement.image.imageUrl,
                        qImageManagement.createdAt)) // createdAt 추가
                .distinct()
                .from(qImageManagement)
                .leftJoin(qImageManagement.image, qImage)
                .leftJoin(qImageManagement.user, qUser)
                .leftJoin(qImageDetail).on(qImageManagement.image.eq(qImageDetail.image)) // ImageDetail 조인
                .leftJoin(qImageDetail.keyword, qKeyword) // Keyword 조인
                .where(builder);

        // 정렬 조건 처리
        switch (requestDTO.getSort()) {
            case 0:
                query.orderBy(qImageManagement.createdAt.desc());
                log.info(">>> 정렬 조건: 최신 순");
                break;
            case 1:
                query.orderBy(qImageManagement.createdAt.asc());
                log.info(">>> 정렬 조건: 오래된 순");
                break;
            case 2:
                query.orderBy(Expressions.numberTemplate(Double.class, "RAND()").asc());
                log.info(">>> 정렬 조건: 랜덤 순");
                break;
        }

        List<ImageListGetResponseDTO> imageList = query
                .offset((requestDTO.getPage() - 1) * requestDTO.getSize())
                .limit(requestDTO.getSize())
                .fetch();

        // 전체 개수 조회 (페이지네이션 적용 전에 실행)
        long totalCount = queryFactory
                .select(qImageManagement.imageManagementId)
                .distinct()
                .from(qImageManagement)
                .leftJoin(qImageManagement.image, qImage)
                .leftJoin(qImageManagement.user, qUser)
                .leftJoin(qImageDetail).on(qImageManagement.image.eq(qImageDetail.image))
                .leftJoin(qImageDetail.keyword, qKeyword)
                .where(builder)
                .fetchCount();

        // 총 페이지 수 계산
        int totalPage = (int) ((totalCount + requestDTO.getSize() - 1) / requestDTO.getSize());

        Map<String, Object> result = new HashMap<>();
        result.put("imageList", imageList);
        result.put("totalPage", totalPage);

        return result;
    }


}
