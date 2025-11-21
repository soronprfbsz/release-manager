package com.ts.rm.domain.releaseversion.repository;

import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import java.util.List;

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
}
