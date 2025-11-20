package com.ts.rm.domain.release.repository;

import com.ts.rm.domain.release.entity.ReleaseVersion;
import java.util.List;

/**
 * ReleaseVersion Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현을 위한 인터페이스
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
     * 고객사별 버전 범위 조회
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param customerId  고객사 ID
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @return 버전 목록
     */
    List<ReleaseVersion> findVersionsBetweenForCustomer(String releaseType, Long customerId,
            String fromVersion, String toVersion);

    /**
     * 버전 정보 업데이트
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @param comment          코멘트
     * @param isInstall        설치본 여부
     * @return 업데이트된 행 수
     */
    long updateVersionInfo(Long releaseVersionId, String comment, Boolean isInstall);
}
