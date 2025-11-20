package com.ts.rm.domain.release.repository;

import com.ts.rm.domain.release.entity.VersionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * VersionHistory Repository
 */
@Repository
public interface VersionHistoryRepository extends JpaRepository<VersionHistory, String> {

    /**
     * 표준 버전으로 조회 (시스템 적용일시 내림차순)
     *
     * @param standardVersion 표준 버전
     * @return 버전 이력 목록
     */
    List<VersionHistory> findByStandardVersionOrderBySystemAppliedAtDesc(String standardVersion);

    /**
     * 시스템 적용일시가 있는 이력 조회 (적용일시 내림차순)
     *
     * @return 적용된 버전 이력 목록
     */
    List<VersionHistory> findBySystemAppliedAtIsNotNullOrderBySystemAppliedAtDesc();
}
