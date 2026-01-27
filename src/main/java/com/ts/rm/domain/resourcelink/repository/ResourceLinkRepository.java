package com.ts.rm.domain.resourcelink.repository;

import com.ts.rm.domain.resourcelink.entity.ResourceLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

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
     * 링크 URL 중복 확인
     */
    boolean existsByLinkUrl(String linkUrl);
}
