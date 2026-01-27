package com.ts.rm.domain.scheduler.controller;

import com.ts.rm.domain.scheduler.dto.ScheduleJobDto;
import com.ts.rm.domain.scheduler.dto.ScheduleJobHistoryDto;
import com.ts.rm.domain.scheduler.service.DynamicScheduler;
import com.ts.rm.domain.scheduler.service.ScheduleJobHistoryService;
import com.ts.rm.domain.scheduler.service.ScheduleJobService;
import com.ts.rm.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ScheduleJob Controller
 *
 * <p>스케줄 작업 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/schedules/jobs")
@RequiredArgsConstructor
public class ScheduleJobController implements ScheduleJobControllerDocs {

    private final ScheduleJobService jobService;
    private final ScheduleJobHistoryService historyService;
    private final DynamicScheduler dynamicScheduler;

    /**
     * 스케줄 작업 목록 조회
     */
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleJobDto.ListResponse>>> getJobs(
            @RequestParam(required = false) String jobGroup) {
        List<ScheduleJobDto.ListResponse> jobs;
        if (jobGroup != null && !jobGroup.isBlank()) {
            jobs = jobService.getJobsByGroup(jobGroup);
        } else {
            jobs = jobService.getJobs();
        }
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    /**
     * 스케줄 작업 상세 조회
     */
    @Override
    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<ScheduleJobDto.Response>> getJob(@PathVariable Long jobId) {
        ScheduleJobDto.Response job = jobService.getJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    /**
     * 스케줄 작업 생성
     */
    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleJobDto.Response>> createJob(
            @Valid @RequestBody ScheduleJobDto.CreateRequest request) {
        ScheduleJobDto.Response job = jobService.createJob(request);
        // 스케줄 등록
        dynamicScheduler.refreshJob(job.jobId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(job));
    }

    /**
     * 스케줄 작업 수정
     */
    @Override
    @PutMapping("/{jobId}")
    public ResponseEntity<ApiResponse<ScheduleJobDto.Response>> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody ScheduleJobDto.UpdateRequest request) {
        ScheduleJobDto.Response job = jobService.updateJob(jobId, request);
        // 스케줄 갱신
        dynamicScheduler.refreshJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    /**
     * 스케줄 작업 삭제
     */
    @Override
    @DeleteMapping("/{jobId}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long jobId) {
        // 스케줄 취소
        dynamicScheduler.cancelJob(jobId);
        jobService.deleteJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 스케줄 작업 활성화/비활성화 토글
     */
    @Override
    @PatchMapping("/{jobId}/toggle")
    public ResponseEntity<ApiResponse<ScheduleJobDto.Response>> toggleJobEnabled(@PathVariable Long jobId) {
        ScheduleJobDto.Response job = jobService.toggleJobEnabled(jobId);
        // 스케줄 갱신
        dynamicScheduler.refreshJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    /**
     * 스케줄 작업 즉시 실행
     */
    @Override
    @PostMapping("/{jobId}/execute")
    public ResponseEntity<ApiResponse<String>> executeJobNow(@PathVariable Long jobId) {
        dynamicScheduler.executeJobNow(jobId);
        return ResponseEntity.ok(ApiResponse.success("작업이 실행 대기열에 추가되었습니다."));
    }

    /**
     * 모든 스케줄 새로고침
     */
    @Override
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refreshAllJobs() {
        dynamicScheduler.loadAllEnabledJobs();
        return ResponseEntity.ok(ApiResponse.success(
                "스케줄이 새로고침되었습니다. 등록된 작업 수: " + dynamicScheduler.getScheduleJobCount()));
    }

    /**
     * 작업별 실행 이력 조회
     */
    @Override
    @GetMapping("/{jobId}/histories")
    public ResponseEntity<ApiResponse<Page<ScheduleJobHistoryDto.ListResponse>>> getJobHistories(
            @PathVariable Long jobId,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ScheduleJobHistoryDto.ListResponse> histories = historyService.getHistoriesByJobId(jobId, pageable);
        return ResponseEntity.ok(ApiResponse.success(histories));
    }

    /**
     * 실행 이력 상세 조회
     */
    @Override
    @GetMapping("/histories/{historyId}")
    public ResponseEntity<ApiResponse<ScheduleJobHistoryDto.Response>> getHistory(@PathVariable Long historyId) {
        ScheduleJobHistoryDto.Response history = historyService.getHistory(historyId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
