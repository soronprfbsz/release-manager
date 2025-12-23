package com.ts.rm.domain.resourcelink.repository;

import com.ts.rm.domain.resourcelink.entity.ResourceLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ResourceLink Repository
 */
public interface ResourceLinkRepository extends JpaRepository<ResourceLink, Long>, ResourceLinkRepositoryCustom {

    /**
     * 전체 리소스 링크 목록 조회 (정렬: sortOrder 오름차순, 생성일시 내림차순)
     */
    List<ResourceLink> findAllByOrderBySortOrderAscCreatedAtDesc();

    /**
     * 링크 카테고리별 리소스 링크 목록 조회 (정렬: sortOrder 오름차순, 생성일시 내림차순)
     */
    List<ResourceLink> findByLinkCategoryOrderBySortOrderAscCreatedAtDesc(String linkCategory);

    /**
     * 링크 카테고리별 최대 sortOrder 조회
     *
     * @param linkCategory 링크 카테고리
     * @return 최대 sortOrder (없으면 0)
     */
    @Query("SELECT COALESCE(MAX(r.sortOrder), 0) FROM ResourceLink r WHERE r.linkCategory = :linkCategory")
    Integer findMaxSortOrderByLinkCategory(@Param("linkCategory") String linkCategory);

    /**
     * 링크 URL 중복 확인
     */
    boolean existsByLinkUrl(String linkUrl);
}
