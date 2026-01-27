package com.ts.rm.domain.scheduler.service;

import com.ts.rm.domain.scheduler.dto.ScheduleJobDto;
import com.ts.rm.domain.scheduler.entity.ScheduleJob;
import com.ts.rm.domain.scheduler.mapper.ScheduleJobDtoMapper;
import com.ts.rm.domain.scheduler.repository.ScheduleJobRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ScheduleJob Service
 *
 * <p>스케줄 작업 관리 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleJobService {

    private final ScheduleJobRepository jobRepository;
    private final ScheduleJobDtoMapper jobMapper;

    /**
     * 스케줄 작업 목록 조회
     */
    public List<ScheduleJobDto.ListResponse> getJobs() {
        log.debug("스케줄 작업 목록 조회");
        List<ScheduleJob> jobs = jobRepository.findAllByOrderByJobGroupAscJobNameAsc();
        return jobMapper.toListResponseList(jobs);
    }

    /**
     * 그룹별 스케줄 작업 목록 조회
     */
    public List<ScheduleJobDto.ListResponse> getJobsByGroup(String jobGroup) {
        log.debug("그룹별 스케줄 작업 목록 조회 - group: {}", jobGroup);
        List<ScheduleJob> jobs = jobRepository.findAllByJobGroupOrderByJobNameAsc(jobGroup);
        return jobMapper.toListResponseList(jobs);
    }

    /**
     * 스케줄 작업 상세 조회
     */
    public ScheduleJobDto.Response getJob(Long jobId) {
        log.debug("스케줄 작업 상세 조회 - jobId: {}", jobId);
        ScheduleJob job = findJobById(jobId);
        return jobMapper.toResponse(job);
    }

    /**
     * 작업명으로 스케줄 작업 조회
     */
    public ScheduleJobDto.Response getJobByName(String jobName) {
        log.debug("작업명으로 스케줄 작업 조회 - jobName: {}", jobName);
        ScheduleJob job = jobRepository.findByJobName(jobName)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "스케줄 작업을 찾을 수 없습니다: " + jobName));
        return jobMapper.toResponse(job);
    }

    /**
     * 스케줄 작업 생성
     */
    @Transactional
    public ScheduleJobDto.Response createJob(ScheduleJobDto.CreateRequest request) {
        log.info("스케줄 작업 생성 - jobName: {}", request.jobName());

        // 작업명 중복 확인
        if (jobRepository.existsByJobName(request.jobName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "이미 존재하는 작업명입니다: " + request.jobName());
        }

        // Cron 표현식 유효성 검증
        validateCronExpression(request.cronExpression());

        // 엔티티 생성
        ScheduleJob job = jobMapper.toEntity(request);

        // 기본값 설정
        if (job.getJobGroup() == null || job.getJobGroup().isBlank()) {
            job.setJobGroup("DEFAULT");
        }
        if (job.getHttpMethod() == null || job.getHttpMethod().isBlank()) {
            job.setHttpMethod("POST");
        }
        if (job.getTimezone() == null || job.getTimezone().isBlank()) {
            job.setTimezone("Asia/Seoul");
        }
        if (job.getIsEnabled() == null) {
            job.setIsEnabled(true);
        }
        if (job.getTimeoutSeconds() == null) {
            job.setTimeoutSeconds(30);
        }
        if (job.getRetryCount() == null) {
            job.setRetryCount(0);
        }
        if (job.getRetryDelaySeconds() == null) {
            job.setRetryDelaySeconds(5);
        }

        // 다음 실행 시각 계산
        job.setNextExecutionAt(calculateNextExecutionTime(job.getCronExpression(), job.getTimezone()));

        ScheduleJob savedJob = jobRepository.save(job);
        log.info("스케줄 작업 생성 완료 - jobId: {}", savedJob.getJobId());

        return jobMapper.toResponse(savedJob);
    }

    /**
     * 스케줄 작업 수정
     */
    @Transactional
    public ScheduleJobDto.Response updateJob(Long jobId, ScheduleJobDto.UpdateRequest request) {
        log.info("스케줄 작업 수정 - jobId: {}", jobId);

        ScheduleJob job = findJobById(jobId);

        // 필드 수정 (null이 아닌 필드만)
        if (request.jobGroup() != null) {
            job.setJobGroup(request.jobGroup());
        }
        if (request.description() != null) {
            job.setDescription(request.description());
        }
        if (request.apiUrl() != null) {
            job.setApiUrl(request.apiUrl());
        }
        if (request.httpMethod() != null) {
            job.setHttpMethod(request.httpMethod());
        }
        if (request.requestBody() != null) {
            job.setRequestBody(request.requestBody());
        }
        if (request.requestHeaders() != null) {
            job.setRequestHeaders(request.requestHeaders());
        }
        if (request.cronExpression() != null) {
            validateCronExpression(request.cronExpression());
            job.setCronExpression(request.cronExpression());
            // Cron 변경 시 다음 실행 시각 재계산
            job.setNextExecutionAt(calculateNextExecutionTime(
                    job.getCronExpression(),
                    request.timezone() != null ? request.timezone() : job.getTimezone()));
        }
        if (request.timezone() != null) {
            job.setTimezone(request.timezone());
        }
        if (request.isEnabled() != null) {
            job.setIsEnabled(request.isEnabled());
        }
        if (request.timeoutSeconds() != null) {
            job.setTimeoutSeconds(request.timeoutSeconds());
        }
        if (request.retryCount() != null) {
            job.setRetryCount(request.retryCount());
        }
        if (request.retryDelaySeconds() != null) {
            job.setRetryDelaySeconds(request.retryDelaySeconds());
        }

        log.info("스케줄 작업 수정 완료 - jobId: {}", jobId);
        return jobMapper.toResponse(job);
    }

    /**
     * 스케줄 작업 삭제
     */
    @Transactional
    public void deleteJob(Long jobId) {
        log.info("스케줄 작업 삭제 - jobId: {}", jobId);

        ScheduleJob job = findJobById(jobId);
        jobRepository.delete(job);

        log.info("스케줄 작업 삭제 완료 - jobId: {}", jobId);
    }

    /**
     * 스케줄 작업 활성화/비활성화 토글
     */
    @Transactional
    public ScheduleJobDto.Response toggleJobEnabled(Long jobId) {
        log.info("스케줄 작업 활성화 토글 - jobId: {}", jobId);

        ScheduleJob job = findJobById(jobId);
        job.setIsEnabled(!job.getIsEnabled());

        if (job.getIsEnabled()) {
            // 활성화 시 다음 실행 시각 재계산
            job.setNextExecutionAt(calculateNextExecutionTime(job.getCronExpression(), job.getTimezone()));
        }

        log.info("스케줄 작업 활성화 토글 완료 - jobId: {}, isEnabled: {}", jobId, job.getIsEnabled());
        return jobMapper.toResponse(job);
    }

    /**
     * 마지막 실행 시각 업데이트
     */
    @Transactional
    public void updateLastExecutedAt(Long jobId) {
        ScheduleJob job = findJobById(jobId);
        job.setLastExecutedAt(LocalDateTime.now());
        job.setNextExecutionAt(calculateNextExecutionTime(job.getCronExpression(), job.getTimezone()));
    }

    /**
     * 활성화된 모든 작업 조회 (스케줄러용)
     */
    public List<ScheduleJob> getEnabledJobs() {
        return jobRepository.findAllByIsEnabledTrue();
    }

    /**
     * Entity 조회
     */
    public ScheduleJob findJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "스케줄 작업을 찾을 수 없습니다: " + jobId));
    }

    /**
     * Cron 표현식 유효성 검증
     */
    private void validateCronExpression(String cronExpression) {
        try {
            CronExpression.parse(cronExpression);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "유효하지 않은 Cron 표현식입니다: " + cronExpression);
        }
    }

    /**
     * 다음 실행 시각 계산
     */
    private LocalDateTime calculateNextExecutionTime(String cronExpression, String timezone) {
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            ZoneId zoneId = ZoneId.of(timezone);
            return cron.next(LocalDateTime.now(zoneId));
        } catch (Exception e) {
            log.warn("다음 실행 시각 계산 실패: {}", e.getMessage());
            return null;
        }
    }
}
