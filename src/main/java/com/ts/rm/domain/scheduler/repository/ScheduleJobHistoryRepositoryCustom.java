package com.ts.rm.domain.scheduler.repository;

import com.ts.rm.domain.scheduler.enums.JobExecutionStatus;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ScheduleJobHistory Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 커스텀 쿼리
 */
public interface ScheduleJobHistoryRepositoryCustom {

    /**
     * 오래된 이력 삭제 (보관 기간 초과)
     *
     * @param cutoffDate 기준 날짜
     * @return 삭제된 건수
     */
    long deleteByStartedAtBefore(LocalDateTime cutoffDate);

    /**
     * 작업별 성공/실패 통계
     *
     * @param jobId 작업 ID
     * @param since 기준 시작일
     * @return 상태별 카운트 Map
     */
    Map<JobExecutionStatus, Long> countByJobIdAndStatusSince(Long jobId, LocalDateTime since);
}
