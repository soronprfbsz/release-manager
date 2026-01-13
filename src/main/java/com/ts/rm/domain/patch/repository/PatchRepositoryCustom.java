package com.ts.rm.domain.patch.repository;

import com.ts.rm.domain.patch.entity.Patch;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Patch Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현을 위한 인터페이스
 */
public interface PatchRepositoryCustom {

    /**
     * 프로젝트별 릴리즈 타입별 최근 N개 패치 조회
     *
     * @param projectId   프로젝트 ID
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param limit       조회 개수
     * @return 최근 패치 목록
     */
    List<Patch> findRecentByProjectIdAndReleaseType(String projectId, String releaseType, int limit);

    /**
     * 패치 목록 조회 (다중 필터링)
     *
     * @param projectId    프로젝트 ID (null이면 전체)
     * @param releaseType  릴리즈 타입 (STANDARD/CUSTOM, null이면 전체)
     * @param customerCode 고객사 코드 (null이면 전체)
     * @param pageable     페이징 정보
     * @return 패치 목록 페이지
     */
    Page<Patch> findAllWithFilters(String projectId, String releaseType, String customerCode, Pageable pageable);
}
