package com.ts.rm.domain.resourcelink.repository;

import com.ts.rm.domain.resourcelink.entity.ResourceLink;
import java.util.List;

/**
 * ResourceLink Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 리소스 링크 쿼리 인터페이스
 */
public interface ResourceLinkRepositoryCustom {

    /**
     * 리소스 링크 목록 조회 (카테고리 필터링 + 키워드 검색)
     *
     * @param linkCategory 링크 카테고리 (null이면 전체)
     * @param keyword 검색 키워드 (링크명, 링크URL, 설명)
     * @return 리소스 링크 목록
     */
    List<ResourceLink> findAllWithFilters(String linkCategory, String keyword);
}
