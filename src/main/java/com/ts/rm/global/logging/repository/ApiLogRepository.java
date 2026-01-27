package com.ts.rm.global.logging.repository;

import com.ts.rm.global.logging.entity.ApiLog;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ApiLog Repository
 *
 * <p>API 로그 데이터 접근 레이어
 */
@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long>, ApiLogRepositoryCustom {

    /**
     * 특정 날짜 이전 로그 수 조회
     *
     * @param cutoffDate 기준 날짜
     * @return 로그 수
     */
    long countByCreatedAtBefore(LocalDateTime cutoffDate);
}
