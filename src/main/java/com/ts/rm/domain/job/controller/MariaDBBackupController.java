package com.ts.rm.domain.job.controller;

import com.ts.rm.domain.job.dto.JobResponse;
import com.ts.rm.domain.job.dto.MariaDBBackupRequest;
import com.ts.rm.domain.job.service.JobStatusManager;
import com.ts.rm.domain.job.service.MariaDBBackupService;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.SecurityUtil;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class MariaDBBackupController implements MariaDBBackupControllerDocs {

    private final MariaDBBackupService backupService;
    private final JobStatusManager jobStatusManager;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyyMMdd_HHmmss");

    @Override
    @PostMapping("/mariadb-backup")
    public ResponseEntity<ApiResponse<JobResponse>> executeBackup(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody MariaDBBackupRequest request) {

        log.info("MariaDB 백업 요청 - host: {}, database: {}",
                request.getHost(), request.getDatabase());

        String createdBy = SecurityUtil.getTokenInfo().email();

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String jobId = "backup_" + timestamp;
        String logFileName = String.format("backup_mariadb_%s.log", timestamp);

        // 백업 파일명 결정: 사용자 입력 or 자동 생성
        String backupFileName;
        if (request.getFileName() != null && !request.getFileName().isBlank()) {
            backupFileName = request.getFileName().trim();
            // .sql 확장자 자동 추가
            if (!backupFileName.toLowerCase().endsWith(".sql")) {
                backupFileName += ".sql";
            }
        } else {
            // 자동 생성: backup_{database}_{timestamp}.sql
            backupFileName = String.format("backup_%s_%s.sql", request.getDatabase(), timestamp);
        }

        // 작업 시작 상태 저장
        JobResponse runningResponse = JobResponse.createRunning(jobId, backupFileName, "logs/" + logFileName);
        jobStatusManager.saveJobStatus(jobId, runningResponse);

        // 비동기 백업 실행
        backupService.executeBackupAsync(request, createdBy, jobId, logFileName, backupFileName);

        return ResponseEntity.ok(ApiResponse.success(runningResponse));
    }

    @Override
    @GetMapping("/mariadb-backup/job-status/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getBackupJobStatus(@PathVariable String id) {

        log.info("백업 작업 상태 조회 요청 - jobId: {}", id);

        JobResponse jobStatus = jobStatusManager.getJobStatus(id);

        if (jobStatus == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "작업 정보를 찾을 수 없습니다: " + id);
        }

        return ResponseEntity.ok(ApiResponse.success(jobStatus));
    }
}
