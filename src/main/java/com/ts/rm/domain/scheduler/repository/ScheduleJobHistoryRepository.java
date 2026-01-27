package com.ts.rm.domain.scheduler.repository;

import com.ts.rm.domain.scheduler.entity.ScheduleJobHistory;
import com.ts.rm.domain.scheduler.enums.JobExecutionStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ScheduleJobHistory Repository
 */
@Repository
public interface ScheduleJobHistoryRepository extends JpaRepository<ScheduleJobHistory, Long>,
        ScheduleJobHistoryRepositoryCustom {

    /**
     * 작업별 실행 이력 조회 (최신순)
     */
    Page<ScheduleJobHistory> findAllByJob_JobIdOrderByStartedAtDesc(Long jobId, Pageable pageable);

    /**
     * 작업별 최근 N개 실행 이력 조회
     */
    List<ScheduleJobHistory> findTop10ByJob_JobIdOrderByStartedAtDesc(Long jobId);

    /**
     * 상태별 실행 이력 조회
     */
    Page<ScheduleJobHistory> findAllByStatusOrderByStartedAtDesc(JobExecutionStatus status, Pageable pageable);

    /**
     * 기간 내 실행 이력 조회
     */
    Page<ScheduleJobHistory> findAllByStartedAtBetweenOrderByStartedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
