package com.ts.rm.domain.job.controller;

import com.ts.rm.domain.job.dto.JobResponse;
import com.ts.rm.domain.job.dto.MariaDBRestoreRequest;
import com.ts.rm.domain.job.service.JobStatusManager;
import com.ts.rm.domain.job.service.MariaDBRestoreService;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.response.ApiResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MariaDB 복원 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class MariaDBRestoreController implements MariaDBRestoreControllerDocs {

    private final MariaDBRestoreService restoreService;
    private final JobStatusManager jobStatusManager;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyyMMdd_HHmmss");

    @Override
    @PostMapping("/mariadb-restore")
    public ResponseEntity<ApiResponse<JobResponse>> executeRestore(@Valid @RequestBody MariaDBRestoreRequest request) {

        log.info("MariaDB 복원 요청 - host: {}, backupFileId: {}",
                request.getHost(), request.getBackupFileId());

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String jobId = "restore_" + timestamp;
        // 로그 파일명에 backupFileId 포함: restore_{backupFileId}_{timestamp}.log
        String logFileName = String.format("restore_%d_%s.log",
                request.getBackupFileId(), timestamp);

        // 작업 시작 상태 저장
        JobResponse runningResponse = JobResponse.createRunning(jobId,
                "backupFileId=" + request.getBackupFileId(), "logs/" + logFileName);
        jobStatusManager.saveJobStatus(jobId, runningResponse);

        // 비동기 복원 실행
        restoreService.executeRestoreAsync(request, jobId, logFileName);

        return ResponseEntity.ok(ApiResponse.success(runningResponse));
    }

    @Override
    @GetMapping("/mariadb-restore/job-status/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> getRestoreJobStatus(@PathVariable String jobId) {

        log.info("복원 작업 상태 조회 요청 - jobId: {}", jobId);

        JobResponse jobStatus = jobStatusManager.getJobStatus(jobId);

        if (jobStatus == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "작업 정보를 찾을 수 없습니다: " + jobId);
        }

        return ResponseEntity.ok(ApiResponse.success(jobStatus));
    }
}
