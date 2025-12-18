package com.ts.rm.domain.releaseversion.repository;

import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.enums.ReleaseCategory;
import java.util.List;
import java.util.Optional;

/**
 * ReleaseVersion Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현을 위한 인터페이스
 * <p>단순 업데이트는 JPA Dirty Checking 사용 (Service에서 엔티티 조회 후 setter 호출)
 */
public interface ReleaseVersionRepositoryCustom {

    /**
     * 버전 범위 조회 (from ~ to)
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @return 버전 목록
     */
    List<ReleaseVersion> findVersionsBetween(String releaseType, String fromVersion, String toVersion);

    /**
     * 릴리즈 타입과 카테고리로 최신 버전 1개 조회
     *
     * @param releaseType     릴리즈 타입 (STANDARD/CUSTOM)
     * @param releaseCategory 릴리즈 카테고리 (INSTALL/PATCH)
     * @return 최신 버전
     */
    Optional<ReleaseVersion> findLatestByReleaseTypeAndCategory(String releaseType, ReleaseCategory releaseCategory);

    /**
     * 릴리즈 타입과 카테고리로 최근 N개 조회
     *
     * @param releaseType     릴리즈 타입 (STANDARD/CUSTOM)
     * @param releaseCategory 릴리즈 카테고리 (INSTALL/PATCH)
     * @param limit           조회 개수
     * @return 최근 버전 목록
     */
    List<ReleaseVersion> findRecentByReleaseTypeAndCategory(String releaseType, ReleaseCategory releaseCategory, int limit);

    /**
     * 프로젝트별 릴리즈 타입과 카테고리로 최신 버전 1개 조회
     *
     * @param projectId       프로젝트 ID
     * @param releaseType     릴리즈 타입 (STANDARD/CUSTOM)
     * @param releaseCategory 릴리즈 카테고리 (INSTALL/PATCH)
     * @return 최신 버전
     */
    Optional<ReleaseVersion> findLatestByProjectIdAndReleaseTypeAndCategory(String projectId, String releaseType, ReleaseCategory releaseCategory);

    /**
     * 프로젝트별 릴리즈 타입과 카테고리로 최근 N개 조회
     *
     * @param projectId       프로젝트 ID
     * @param releaseType     릴리즈 타입 (STANDARD/CUSTOM)
     * @param releaseCategory 릴리즈 카테고리 (INSTALL/PATCH)
     * @param limit           조회 개수
     * @return 최근 버전 목록
     */
    List<ReleaseVersion> findRecentByProjectIdAndReleaseTypeAndCategory(String projectId, String releaseType, ReleaseCategory releaseCategory, int limit);

    /**
     * 버전 범위 내 미승인 버전 조회 (from ~ to)
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @return 미승인 버전 목록 (isApproved = false)
     */
    List<ReleaseVersion> findUnapprovedVersionsBetween(String releaseType, String fromVersion, String toVersion);
}
