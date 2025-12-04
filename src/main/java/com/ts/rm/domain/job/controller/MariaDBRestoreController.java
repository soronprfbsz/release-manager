package com.ts.rm.domain.job.controller;

import com.ts.rm.domain.job.dto.MariaDBRestoreRequest;
import com.ts.rm.domain.job.dto.JobResponse;
import com.ts.rm.domain.job.service.JobStatusManager;
import com.ts.rm.domain.job.service.MariaDBRestoreService;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.response.ApiResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MariaDB 복원 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "MariaDB 복원", description = "MariaDB 복원 API")
public class MariaDBRestoreController {

    private final MariaDBRestoreService restoreService;
    private final JobStatusManager jobStatusManager;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyyMMdd_HHmmss");

    /**
     * MariaDB 복원 실행 (비동기)
     *
     * @param request 복원 요청 정보
     * @return 복원 작업 시작 응답
     */
    @PostMapping("/mariadb-restore")
    @Operation(summary = "MariaDB 복원", description = "백업 파일을 사용하여 MariaDB 서버의 데이터베이스를 비동기로 복원합니다.")
    public ResponseEntity<ApiResponse<JobResponse>> executeRestore(
            @Valid @RequestBody MariaDBRestoreRequest request) {

        log.info("MariaDB 복원 요청 - host: {}, backupFile: {}",
                request.getHost(), request.getBackupFileName());

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String jobId = "restore_" + timestamp;
        String logFileName = String.format("restore_mariadb_%s.log", timestamp);

        // 작업 시작 상태 저장
        JobResponse runningResponse = JobResponse.createRunning(jobId,
                request.getBackupFileName(), "logs/" + logFileName);
        jobStatusManager.saveJobStatus(jobId, runningResponse);

        // 비동기 복원 실행
        restoreService.executeRestoreAsync(request, jobId, logFileName);

        return ResponseEntity.ok(ApiResponse.success(runningResponse));
    }

    /**
     * 복원 작업 상태 조회
     *
     * @param jobId 작업 ID
     * @return 작업 상태 응답
     */
    @GetMapping("/mariadb-restore/job-status/{jobId}")
    @Operation(summary = "복원 작업 상태 조회", description = "복원 작업의 현재 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<JobResponse>> getRestoreJobStatus(
            @PathVariable String jobId) {

        log.info("복원 작업 상태 조회 요청 - jobId: {}", jobId);

        JobResponse jobStatus = jobStatusManager.getJobStatus(jobId);

        if (jobStatus == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "작업 정보를 찾을 수 없습니다: " + jobId);
        }

        return ResponseEntity.ok(ApiResponse.success(jobStatus));
    }
}
