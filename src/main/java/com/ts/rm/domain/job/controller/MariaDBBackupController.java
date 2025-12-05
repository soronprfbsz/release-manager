package com.ts.rm.domain.job.controller;

import com.ts.rm.domain.job.dto.MariaDBBackupRequest;
import com.ts.rm.domain.job.dto.JobResponse;
import com.ts.rm.domain.job.service.JobStatusManager;
import com.ts.rm.domain.job.service.MariaDBBackupService;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MariaDB 백업 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "작업", description = "작업 관리 API")
public class MariaDBBackupController {

    private final MariaDBBackupService backupService;
    private final JobStatusManager jobStatusManager;
    private final JwtTokenProvider jwtTokenProvider;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyyMMdd_HHmmss");

    /**
     * MariaDB 백업 실행 (비동기)
     *
     * @param request 백업 요청 정보
     * @return 백업 작업 시작 응답
     */
    @PostMapping("/mariadb-backup")
    @Operation(summary = "MariaDB 백업", description = "MariaDB 서버의 데이터베이스를 비동기로 백업합니다.")
    public ResponseEntity<ApiResponse<JobResponse>> executeBackup(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody MariaDBBackupRequest request) {

        log.info("MariaDB 백업 요청 - host: {}, database: {}",
                request.getHost(), request.getDatabase());

        String token = authorizationHeader.substring(7);
        String createdBy = jwtTokenProvider.getEmail(token);

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String jobId = "backup_" + timestamp;
        String logFileName = String.format("backup_mariadb_%s.log", timestamp);

        // 작업 시작 상태 저장
        String backupFileName = String.format("backup_%s_%s.sql", request.getDatabase(), timestamp);
        JobResponse runningResponse = JobResponse.createRunning(jobId, backupFileName, "logs/" + logFileName);
        jobStatusManager.saveJobStatus(jobId, runningResponse);

        // 비동기 백업 실행
        backupService.executeBackupAsync(request, createdBy, jobId, logFileName);

        return ResponseEntity.ok(ApiResponse.success(runningResponse));
    }

    /**
     * 백업 작업 상태 조회
     *
     * @param jobId 작업 ID
     * @return 작업 상태 응답
     */
    @GetMapping("/mariadb-backup/job-status/{jobId}")
    @Operation(summary = "백업 작업 상태 조회", description = "백업 작업의 현재 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<JobResponse>> getBackupJobStatus(
            @PathVariable String jobId) {

        log.info("백업 작업 상태 조회 요청 - jobId: {}", jobId);

        JobResponse jobStatus = jobStatusManager.getJobStatus(jobId);

        if (jobStatus == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "작업 정보를 찾을 수 없습니다: " + jobId);
        }

        return ResponseEntity.ok(ApiResponse.success(jobStatus));
    }
}
