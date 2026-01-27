package com.ts.rm.domain.scheduler.service;

import com.ts.rm.domain.scheduler.dto.ScheduleJobHistoryDto;
import com.ts.rm.domain.scheduler.entity.ScheduleJob;
import com.ts.rm.domain.scheduler.entity.ScheduleJobHistory;
import com.ts.rm.domain.scheduler.enums.JobExecutionStatus;
import com.ts.rm.domain.scheduler.mapper.ScheduleJobHistoryDtoMapper;
import com.ts.rm.domain.scheduler.repository.ScheduleJobHistoryRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ScheduleJobHistory Service
 *
 * <p>스케줄 실행 이력 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleJobHistoryService {

    private final ScheduleJobHistoryRepository historyRepository;
    private final ScheduleJobHistoryDtoMapper historyMapper;

    /**
     * 작업별 실행 이력 조회 (페이징)
     */
    public Page<ScheduleJobHistoryDto.ListResponse> getHistoriesByJobId(Long jobId, Pageable pageable) {
        log.debug("작업별 실행 이력 조회 - jobId: {}", jobId);
        Page<ScheduleJobHistory> histories = historyRepository
                .findAllByJob_JobIdOrderByStartedAtDesc(jobId, pageable);
        return histories.map(historyMapper::toListResponse);
    }

    /**
     * 실행 이력 상세 조회
     */
    public ScheduleJobHistoryDto.Response getHistory(Long historyId) {
        log.debug("실행 이력 상세 조회 - historyId: {}", historyId);
        ScheduleJobHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "실행 이력을 찾을 수 없습니다: " + historyId));
        return historyMapper.toResponse(history);
    }

    /**
     * 작업별 최근 실행 이력 조회
     */
    public List<ScheduleJobHistoryDto.ListResponse> getRecentHistories(Long jobId) {
        log.debug("작업별 최근 실행 이력 조회 - jobId: {}", jobId);
        List<ScheduleJobHistory> histories = historyRepository
                .findTop10ByJob_JobIdOrderByStartedAtDesc(jobId);
        return historyMapper.toListResponseList(histories);
    }

    /**
     * 실행 이력 생성 (실행 시작)
     */
    @Transactional
    public ScheduleJobHistory createHistory(ScheduleJob job, int attemptNumber) {
        log.debug("실행 이력 생성 - jobId: {}, attemptNumber: {}", job.getJobId(), attemptNumber);

        ScheduleJobHistory history = ScheduleJobHistory.builder()
                .job(job)
                .jobName(job.getJobName())
                .startedAt(LocalDateTime.now())
                .status(JobExecutionStatus.RUNNING)
                .attemptNumber(attemptNumber)
                .build();

        return historyRepository.save(history);
    }

    /**
     * 실행 이력 완료 처리
     */
    @Transactional
    public void completeHistory(ScheduleJobHistory history, JobExecutionStatus status,
            Integer responseCode, String responseBody, String errorMessage) {
        log.debug("실행 이력 완료 - historyId: {}, status: {}", history.getHistoryId(), status);
        history.complete(status, responseCode, responseBody, errorMessage);
        historyRepository.save(history);
    }

    /**
     * 오래된 이력 삭제
     *
     * @param retentionDays 보관 기간 (일)
     * @return 삭제된 건수
     */
    @Transactional
    public int deleteOldHistories(int retentionDays) {
        log.info("오래된 실행 이력 삭제 - retentionDays: {}", retentionDays);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        long deletedCount = historyRepository.deleteByStartedAtBefore(cutoffDate);
        log.info("오래된 실행 이력 삭제 완료 - deletedCount: {}", deletedCount);
        return (int) deletedCount;
    }
}
