package com.ts.rm.domain.publishing.repository;

import com.ts.rm.domain.publishing.entity.Publishing;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Publishing Repository
 */
@Repository
public interface PublishingRepository extends JpaRepository<Publishing, Long>, PublishingRepositoryCustom {

    /**
     * 카테고리별 조회 (정렬순서 오름차순, 생성일시 내림차순)
     */
    List<Publishing> findByPublishingCategoryOrderBySortOrderAscCreatedAtDesc(String publishingCategory);

    /**
     * 고객사별 퍼블리싱 조회
     */
    List<Publishing> findByCustomer_CustomerIdOrderBySortOrderAscCreatedAtDesc(Long customerId);

    /**
     * 카테고리별 최대 sortOrder 조회 (자동 채번용)
     *
     * @param publishingCategory 퍼블리싱 카테고리
     * @return 최대 sortOrder (없으면 0)
     */
    @Query("SELECT COALESCE(MAX(p.sortOrder), 0) FROM Publishing p WHERE p.publishingCategory = :publishingCategory")
    Integer findMaxSortOrderByPublishingCategory(@Param("publishingCategory") String publishingCategory);

    /**
     * 퍼블리싱명 중복 확인
     */
    boolean existsByPublishingName(String publishingName);

    /**
     * 퍼블리싱명 중복 확인 (자기 자신 제외)
     */
    boolean existsByPublishingNameAndPublishingIdNot(String publishingName, Long publishingId);
}
