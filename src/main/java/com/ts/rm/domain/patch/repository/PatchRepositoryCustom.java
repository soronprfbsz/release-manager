package com.ts.rm.domain.patch.repository;

import com.ts.rm.domain.patch.entity.Patch;
import java.util.List;

/**
 * Patch Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현을 위한 인터페이스
 */
public interface PatchRepositoryCustom {

    /**
     * 릴리즈 타입별 최근 N개 패치 조회
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param limit       조회 개수
     * @return 최근 패치 목록
     */
    List<Patch> findRecentByReleaseType(String releaseType, int limit);

    /**
     * 프로젝트별 릴리즈 타입별 최근 N개 패치 조회
     *
     * @param projectId   프로젝트 ID
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param limit       조회 개수
     * @return 최근 패치 목록
     */
    List<Patch> findRecentByProjectIdAndReleaseType(String projectId, String releaseType, int limit);
}
