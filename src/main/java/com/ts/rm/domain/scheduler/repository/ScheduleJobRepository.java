package com.ts.rm.domain.scheduler.repository;

import com.ts.rm.domain.scheduler.entity.ScheduleJob;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ScheduleJob Repository
 */
@Repository
public interface ScheduleJobRepository extends JpaRepository<ScheduleJob, Long> {

    /**
     * 작업명으로 조회
     */
    Optional<ScheduleJob> findByJobName(String jobName);

    /**
     * 작업명 존재 여부 확인
     */
    boolean existsByJobName(String jobName);

    /**
     * 활성화된 작업 목록 조회
     */
    List<ScheduleJob> findAllByIsEnabledTrue();

    /**
     * 그룹별 작업 목록 조회
     */
    List<ScheduleJob> findAllByJobGroupOrderByJobNameAsc(String jobGroup);

    /**
     * 전체 작업 목록 조회 (정렬)
     */
    List<ScheduleJob> findAllByOrderByJobGroupAscJobNameAsc();
}
