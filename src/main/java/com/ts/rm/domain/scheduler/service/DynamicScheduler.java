package com.ts.rm.domain.scheduler.service;

import com.ts.rm.domain.scheduler.entity.ScheduleJob;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

/**
 * Dynamic Scheduler
 *
 * <p>데이터베이스 기반 동적 스케줄러
 * <p>활성화된 작업들을 로드하여 Cron 표현식에 따라 실행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicScheduler {

    private final TaskScheduler taskScheduler;
    private final ScheduleJobService jobService;
    private final ScheduleJobExecutor jobExecutor;

    /**
     * 활성화된 스케줄 작업 맵 (jobId -> ScheduledFuture)
     */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * 애플리케이션 시작 시 스케줄 로드
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("동적 스케줄러 초기화 시작");
        loadAllEnabledJobs();
        log.info("동적 스케줄러 초기화 완료 - 등록된 작업 수: {}", scheduledTasks.size());
    }

    /**
     * 애플리케이션 종료 시 스케줄 정리
     */
    @PreDestroy
    public void onShutdown() {
        log.info("동적 스케줄러 종료 - 모든 스케줄 취소");
        scheduledTasks.values().forEach(future -> future.cancel(false));
        scheduledTasks.clear();
    }

    /**
     * 모든 활성화된 작업 로드
     */
    public void loadAllEnabledJobs() {
        // 기존 스케줄 모두 취소
        scheduledTasks.values().forEach(future -> future.cancel(false));
        scheduledTasks.clear();

        // 활성화된 작업 로드
        jobService.getEnabledJobs().forEach(this::scheduleJob);
    }

    /**
     * 특정 작업 스케줄 등록/갱신
     */
    public void scheduleJob(ScheduleJob job) {
        // 기존 스케줄 취소
        cancelJob(job.getJobId());

        if (!job.getIsEnabled()) {
            log.debug("비활성화된 작업 스킵 - jobId: {}", job.getJobId());
            return;
        }

        try {
            CronTrigger trigger = new CronTrigger(
                    job.getCronExpression(),
                    ZoneId.of(job.getTimezone())
            );

            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> executeJob(job),
                    trigger
            );

            scheduledTasks.put(job.getJobId(), future);
            log.info("스케줄 작업 등록 - jobId: {}, jobName: {}, cron: {}",
                    job.getJobId(), job.getJobName(), job.getCronExpression());

        } catch (Exception e) {
            log.error("스케줄 작업 등록 실패 - jobId: {}, error: {}", job.getJobId(), e.getMessage());
        }
    }

    /**
     * 특정 작업 스케줄 취소
     */
    public void cancelJob(Long jobId) {
        ScheduledFuture<?> future = scheduledTasks.remove(jobId);
        if (future != null) {
            future.cancel(false);
            log.info("스케줄 작업 취소 - jobId: {}", jobId);
        }
    }

    /**
     * 작업 즉시 실행 (수동 트리거)
     */
    public void executeJobNow(Long jobId) {
        ScheduleJob job = jobService.findJobById(jobId);
        log.info("스케줄 작업 즉시 실행 - jobId: {}, jobName: {}", jobId, job.getJobName());
        taskScheduler.schedule(() -> executeJob(job), java.time.Instant.now());
    }

    /**
     * 작업 실행
     */
    private void executeJob(ScheduleJob job) {
        try {
            // 최신 작업 정보 로드 (비활성화 체크)
            ScheduleJob latestJob = jobService.findJobById(job.getJobId());
            if (!latestJob.getIsEnabled()) {
                log.debug("비활성화된 작업 실행 스킵 - jobId: {}", job.getJobId());
                return;
            }

            jobExecutor.execute(latestJob);

        } catch (Exception e) {
            log.error("스케줄 작업 실행 중 오류 - jobId: {}, error: {}", job.getJobId(), e.getMessage(), e);
        }
    }

    /**
     * 스케줄 갱신 (작업 수정 시 호출)
     */
    public void refreshJob(Long jobId) {
        try {
            ScheduleJob job = jobService.findJobById(jobId);
            scheduleJob(job);
        } catch (Exception e) {
            log.warn("스케줄 갱신 실패 - jobId: {}, error: {}", jobId, e.getMessage());
            cancelJob(jobId);
        }
    }

    /**
     * 등록된 스케줄 수 조회
     */
    public int getScheduleJobCount() {
        return scheduledTasks.size();
    }
}
