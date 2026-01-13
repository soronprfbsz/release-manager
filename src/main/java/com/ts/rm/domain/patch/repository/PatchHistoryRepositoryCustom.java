package com.ts.rm.domain.patch.repository;

import com.ts.rm.domain.patch.entity.PatchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PatchHistory Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 인터페이스
 */
public interface PatchHistoryRepositoryCustom {

    /**
     * 프로젝트/고객사별 패치 이력 조회 (필터링 + 페이징)
     *
     * @param projectId  프로젝트 ID (null이면 전체)
     * @param customerId 고객사 ID (null이면 전체)
     * @param pageable   페이징 정보
     * @return 패치 이력 페이지
     */
    Page<PatchHistory> findAllWithFilters(String projectId, Long customerId, Pageable pageable);
}
