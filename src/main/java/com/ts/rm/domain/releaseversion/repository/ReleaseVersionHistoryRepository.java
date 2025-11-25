package com.ts.rm.domain.releaseversion.repository;

import com.ts.rm.domain.releaseversion.entity.ReleaseVersionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ReleaseVersionHistory Repository
 *
 * <p>릴리즈 버전 이력 데이터 접근 인터페이스
 */
@Repository
public interface ReleaseVersionHistoryRepository extends JpaRepository<ReleaseVersionHistory, String> {

    /**
     * 표준 버전으로 조회 (시스템 적용일시 내림차순)
     *
     * @param standardVersion 표준 버전
     * @return 릴리즈 버전 이력 목록
     */
    List<ReleaseVersionHistory> findByStandardVersionOrderBySystemAppliedAtDesc(String standardVersion);

    /**
     * 시스템 적용일시가 있는 이력 조회 (적용일시 내림차순)
     *
     * @return 적용된 릴리즈 버전 이력 목록
     */
    List<ReleaseVersionHistory> findBySystemAppliedAtIsNotNullOrderBySystemAppliedAtDesc();
}
